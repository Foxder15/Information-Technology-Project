import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class TopKHUI {

    // Transaction dataset (items and their utilities)
    public static List<Map<String, Integer>> modelTransaction(String fileName) {
        List<Map<String, Integer>> transactions = new ArrayList<>();
        String thisLine;
        try (BufferedReader myInput = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))) {
            while ((thisLine = myInput.readLine()) != null) {
                if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}
                String split[] = thisLine.split(":");
				String items[] = split[0].split(" ");
				String utilityValues[] = split[2].split(" ");
                HashMap<String, Integer> map = new HashMap<>();
                for (int i = 0; i < items.length; i++) {
                    map.put(items[i], Integer.parseInt(utilityValues[i]));
                }
                transactions.add(map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
        return transactions;
    }

    // Function to calculate the utility of an itemset
    public static int calculateUtility(Set<String> itemset, Map<String, Integer> transaction) {
        return itemset.stream().mapToInt(item -> transaction.getOrDefault(item, 0)).sum();
    }

    // Function to generate all possible itemsets
    public static List<Set<String>> generateItemsets(Map<String, Integer> transaction) {
        List<String> items = new ArrayList<>(transaction.keySet());
        List<Set<String>> itemsets = new ArrayList<>();
        int n = items.size();
        for (int i = 1; i < (1 << n); i++) {
            Set<String> itemset = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    itemset.add(items.get(j));
                }
            }
            itemsets.add(itemset);
        }
        return itemsets;
    }

    // Function to mine Top-k High Utility Itemsets
    public static List<Map.Entry<Set<String>, Integer>> topKHUI(List<Map<String, Integer>> transactions, int k) {
        Map<Set<String>, Integer> itemsets = new HashMap<>();

        // Generate and evaluate all itemsets across all transactions
        for (Map<String, Integer> transaction : transactions) {
            for (Set<String> itemset : generateItemsets(transaction)) {
                int utility = calculateUtility(itemset, transaction);
                itemsets.merge(itemset, utility, Integer::sum);
            }
        }

        // Sort itemsets by utility in descending order
        List<Map.Entry<Set<String>, Integer>> sortedItemsets = new ArrayList<>(itemsets.entrySet());
        sortedItemsets.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Return the top-k itemsets
        return sortedItemsets.subList(0, Math.min(k, sortedItemsets.size()));
    }

    // Main method
    public static void main(String[] args) {
        int k = 3; // Find the top 3 itemsets
        List<Map<String, Integer>> transactions = modelTransaction("retail_negative.txt");
        List<Map.Entry<Set<String>, Integer>> topItemsets = topKHUI(transactions, k);

        System.out.println("Top-k High Utility Itemsets:");
        for (Map.Entry<Set<String>, Integer> entry : topItemsets) {
            System.out.println("Itemset: " + entry.getKey() + ", Utility: " + entry.getValue());
        }


    }
}
