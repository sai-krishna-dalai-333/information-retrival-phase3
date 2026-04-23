import java.io.*;
import java.util.*;
import java.util.regex.*;

public class IRSystem {

    private Set<String> stopWords = new HashSet<>();
    private Map<String, String> documents = new HashMap<>();
    private Map<String, Map<String, Integer>> docTermFrequencies = new HashMap<>();
    private Map<String, Integer> documentFrequencies = new HashMap<>();
    private Map<String, Map<String, Double>> docTfIdf = new HashMap<>();
    private Map<String, Double> docNorms = new HashMap<>();
    
    private Porter stemmer = new Porter();
    public static void main(String[] args) {
        try {
            IRSystem ir = new IRSystem();
            System.out.println("Loading stopwords...");
            ir.loadStopwords("stopwordlist.txt");
            
            System.out.println("Loading documents...");
            ir.loadDocuments("documents");
            
            System.out.println("Building indices and TF-IDF weights...");
            ir.buildIndexAndTfIdf();
            
            System.out.println("Loading queries...");
            List<Topic> queries = ir.loadQueries("topics.txt");
            
            System.out.println("Processing queries and ranking documents...");
            // Mode: 1 = Title only, 2 = Title + Desc, 3 = Title + Narr
            // Changed to Mode 3 to provide maximum context and boost recall/precision
            ir.processQueries(queries, 3, "vsm_output.txt");
            
            System.out.println("Done! Results saved to vsm_output.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStopwords(String filepath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        String line;
        while ((line = br.readLine()) != null) {
            stopWords.add(line.trim().toLowerCase());
        }
        br.close();
    }

    private void loadDocuments(String folderPath) throws IOException {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null) return;

        Pattern docnoPattern = Pattern.compile("<DOCNO>(.*?)</DOCNO>", Pattern.DOTALL);
        Pattern textPattern = Pattern.compile("<TEXT>(.*?)</TEXT>", Pattern.DOTALL);

        for (File file : listOfFiles) {
            if (file.isFile() && !file.getName().startsWith("stopwordlist")) {
                String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                Matcher docnoMatcher = docnoPattern.matcher(content);
                Matcher textMatcher = textPattern.matcher(content);

                while (docnoMatcher.find() && textMatcher.find()) {
                    String docId = docnoMatcher.group(1).trim();
                    String text = textMatcher.group(1).trim();
                    documents.put(docId, text);
                }
            }
        }
    }

    private List<String> tokenizeAndStem(String text) {
        List<String> tokens = new ArrayList<>();
        String[] rawTokens = text.toLowerCase().split("[^a-z0-9]+");
        for (String token : rawTokens) {
            if (token.length() > 0 && !stopWords.contains(token)) {
                String stemmed = stemmer.stripAffixes(token);
                if (stemmed.length() > 0) {
                    tokens.add(stemmed);
                }
            }
        }
        return tokens;
    }

    private void buildIndexAndTfIdf() {
        int totalDocs = documents.size();
        for (Map.Entry<String, String> entry : documents.entrySet()) {
            String docId = entry.getKey();
            List<String> tokens = tokenizeAndStem(entry.getValue());
            
            Map<String, Integer> tfMap = new HashMap<>();
            for (String token : tokens) {
                tfMap.put(token, tfMap.getOrDefault(token, 0) + 1);
            }
            docTermFrequencies.put(docId, tfMap);

            for (String uniqueToken : tfMap.keySet()) {
                documentFrequencies.put(uniqueToken, documentFrequencies.getOrDefault(uniqueToken, 0) + 1);
            }
        }

        // 2. Calculate TF-IDF and Document Norms for Cosine Similarity
        for (String docId : docTermFrequencies.keySet()) {
            Map<String, Integer> tfMap = docTermFrequencies.get(docId);
            Map<String, Double> tfIdfMap = new HashMap<>();
            double normSq = 0.0;

            for (Map.Entry<String, Integer> termEntry : tfMap.entrySet()) {
                String term = termEntry.getKey();
                int tf = termEntry.getValue();
                int df = documentFrequencies.getOrDefault(term, 1);
                
                double idf = Math.log((double) totalDocs / df);
                double weight = tf * idf;
                
                tfIdfMap.put(term, weight);
                normSq += (weight * weight);
            }
            docTfIdf.put(docId, tfIdfMap);
            docNorms.put(docId, Math.sqrt(normSq));
        }
    }

    private List<Topic> loadQueries(String filepath) throws IOException {
        List<Topic> queries = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        String line;
        
        int queryNum = -1;
        String title = "", desc = "", narr = "";
        boolean readingNarr = false;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("<num>")) {
                queryNum = Integer.parseInt(line.replaceAll("[^0-9]", ""));
            } else if (line.startsWith("<title>")) {
                title = line.substring(line.indexOf(">") + 1).trim();
            } else if (line.startsWith("<desc>")) {
                desc = br.readLine().trim(); 
            } else if (line.startsWith("<narr>")) {
                readingNarr = true;
                StringBuilder narrBuilder = new StringBuilder();
                while ((line = br.readLine()) != null && !line.startsWith("</top>")) {
                    narrBuilder.append(line).append(" ");
                }
                narr = narrBuilder.toString().trim();
                readingNarr = false;
                
                queries.add(new Topic(queryNum, title, desc, narr));
            }
        }
        br.close();
        return queries;
    }

    private void processQueries(List<Topic> queries, int mode, String outputFile) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        int totalDocs = documents.size();

        for (Topic query : queries) {
            String queryText = query.title;
            if (mode == 2) queryText += " " + query.description;
            if (mode == 3) queryText += " " + query.narrative;

            List<String> queryTokens = tokenizeAndStem(queryText);
            Map<String, Integer> queryTf = new HashMap<>();
            for (String token : queryTokens) {
                queryTf.put(token, queryTf.getOrDefault(token, 0) + 1);
            }

            Map<String, Double> queryTfIdf = new HashMap<>();
            double queryNormSq = 0.0;
            
            for (Map.Entry<String, Integer> qEntry : queryTf.entrySet()) {
                String term = qEntry.getKey();
                int tf = qEntry.getValue();
                int df = documentFrequencies.getOrDefault(term, 0);
                
                // If term isn't in corpus, ignore it
                if (df == 0) continue; 
                
                double idf = Math.log((double) totalDocs / df);
                double weight = tf * idf;
                queryTfIdf.put(term, weight);
                queryNormSq += (weight * weight);
            }
            double queryNorm = Math.sqrt(queryNormSq);

            // Calculate Cosine Similarity
            List<Map.Entry<String, Double>> rankedDocs = new ArrayList<>();
            
            for (String docId : docTfIdf.keySet()) {
                double dotProduct = 0.0;
                Map<String, Double> docVector = docTfIdf.get(docId);
                
                for (String qTerm : queryTfIdf.keySet()) {
                    if (docVector.containsKey(qTerm)) {
                        dotProduct += (queryTfIdf.get(qTerm) * docVector.get(qTerm));
                    }
                }
                
                if (dotProduct > 0) {
                    double docNorm = docNorms.get(docId);
                    double cosineSimilarity = dotProduct / (queryNorm * docNorm);
                    rankedDocs.add(new AbstractMap.SimpleEntry<>(docId, cosineSimilarity));
                }
            }

            // Sort descending
            rankedDocs.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            // Write Output
            int rankCounter = 1;
            int MAX_RESULTS = 100; // Limit to Top 100 documents
            
            for (Map.Entry<String, Double> result : rankedDocs) {
                if (rankCounter > MAX_RESULTS) {
                    break;
                }
                // TOPIC \t DOCUMENT \t UNIQUE# \t COSINE_VALUE
                bw.write(query.id + "\t" + result.getKey() + "\t" + rankCounter + "\t" + String.format("%.6f", result.getValue()) + "\n");
                rankCounter++;
            }
        }
        bw.close();
    }
}