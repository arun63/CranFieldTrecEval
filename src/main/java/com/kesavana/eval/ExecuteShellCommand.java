package com.kesavana.eval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExecuteShellCommand {
	
	private static final String DECIMAL_PATTERN = "[0](\\.[0-9][0-9][0-9][0-9]?)";

	private static Pattern pattern = Pattern.compile(DECIMAL_PATTERN);
	private static Matcher matcher;
	
	public static String getCmdOptions[] = {"iprec_at_recall", "map", "recall"};
	public static String ns1[] = {"standard", "simple", "stop", "whitespace"};
	public static String ns2[] = {"bm25", "idf", "bm25_idf", "dfrL", "bm25_idf_dfrL", "bm25_idf_dfrL_LM"};
	
	
	public static String trec = "/trec_eval.9.0/trec_eval";
	public static String qRel = "test/QRelsCorrectedforTRECeval"+ " ";
	public static String resFolder = "test/results";
	
	

	public static void main(String[] args) {

		ExecuteShellCommand obj = new ExecuteShellCommand();

		String currDir= System.getProperty("user.dir");
		
		String outputFolder = currDir + "/output";
		String outputFile = null;
		createDirectories(outputFolder);
		
		
		
		ArrayList<ArrayList<String>> allVarNames = new ArrayList<>();
		
		ArrayList<String> f1 = obj.variableFileNames(ns1, new String[] {ns2[0]});
		ArrayList<String> f2 = obj.variableFileNames(ns1, new String[] {ns2[1]});
		ArrayList<String> f3 = obj.variableFileNames(ns1, new String[] {ns2[2]});
		ArrayList<String> f4 = obj.variableFileNames(new String[] {ns1[0]}, ns2);
		allVarNames.add(f1);
		allVarNames.add(f2);
		allVarNames.add(f3);
		allVarNames.add(f4);
		
		ArrayList<List<String>> tempRes;
		//obj.executeCommand("cd " + currDir);

		for(String getCmd : getCmdOptions) {
			int i = 1;
			for(ArrayList<String> files : allVarNames) {	
				System.out.println(files);
				tempRes = new ArrayList<>();
				ArrayList<String> tFiles = new ArrayList<>();
				for(String file: files) {						
					String command = "." + trec + " -m " + getCmd + " " + qRel +  resFolder + "/" + file;
					System.out.println(command);
					String output = obj.executeCommand(command);
					List<String> lst = obj.getMatchedStringOutput(output);
					/*if(resList.isEmpty()) {
					//	resList.add(obj.firstColumn(getCmdOptions[2]));
					}*/
					tempRes.add(lst);
				}
				//System.out.println(tempRes);
				List<List<String>> resultant = new ArrayList<List<String>>();
				tFiles.add("#");
				tFiles.addAll(files);
				resultant.add(tFiles);
				resultant.addAll(transpose(tempRes));
				//System.out.println(resultant);
				try {
					outputFile = outputFolder + "/" + getCmd + "_file_" + i + ".dat"  /*+new SimpleDateFormat("yyyyMMddHHmm'.dat'").format(new Date())*/;
					checkFileExists(outputFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
				obj.writeToFile(outputFile, resultant);
				i++;
			}		
		}
		System.out.println("Check the files in the output folder: " + outputFolder);
		
	}
	
	
	public ArrayList<String> firstColumn(String name) {
		ArrayList<String> first = new ArrayList<>();
		if(name.equals(getCmdOptions[0]) || name.equals(getCmdOptions[1])) {
			first.add("0");
		} else if(name.equals(getCmdOptions[2])) {
			for(int i = 0; i < 10; i++) {
				first.add("0." + i + "0");
			}
			first.add("1.00");
		}
		return first;
	}
	
	public void displayOutput(Map<String, List<String>> resMap) {
	 for (Entry<String, List<String>> resultant : resMap.entrySet()) {
			 System.out.println(resultant.getKey() + ":::" + resultant.getValue().toString());
		 }
	 }
	
	public void writeToFile(String fileName, List<List<String>> results) {
		
		File f = new File (fileName);
		FileWriter fw = null;
		BufferedWriter bw = null;
        if (!f.exists()) {
            try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
		try {
			fw = new FileWriter(f.getAbsoluteFile());
			bw = new BufferedWriter(fw);
	        for(List<String> result : results) {
	        		for(String res : result) {
	        			bw.write(res + " ");
	        		}
	        		bw.write(System.getProperty("line.separator"));
	        }
	        bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void createDirectories(String path) {
		Path p = Paths.get(path);
        if (!Files.exists(p)) {
            try {
            	    Files.createDirectories(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
    
    public static void checkFileExists(String fileName) throws IOException {
    		File f = new File(fileName);
        if(!f.exists() && !f.isDirectory()) { 
        		Files.createFile(Paths.get(fileName));
        }
    }
	
	public static <T> List<List<T>> transpose(List<List<T>> table) {
        List<List<T>> ret = new ArrayList<List<T>>();
        final int N = table.get(0).size();
        for (int i = 0; i < N; i++) {
            List<T> col = new ArrayList<T>();
            for (List<T> row : table) {
                col.add(row.get(i));
            }
            ret.add(col);
        }
        return ret;
    }

	private String executeCommand(String command) {
		StringBuffer output = new StringBuffer();
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader =
                           new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();
	}

	public List<String> getMatchedStringOutput(String msg) {

		List<String> ipList = new ArrayList<String>();
		if (msg == null || msg.equals("")) {
			return ipList;
		}
		
		matcher = pattern.matcher(msg);
		while (matcher.find()) {
			ipList.add(matcher.group(0));
		}
		return ipList;
	}

	public ArrayList<String> variableFileNames(String[] analyzers, String[] similarities) {
		ArrayList<String> variableNames = new ArrayList<>();

		for(String var1: analyzers) {
			for(String var2: similarities) {
				variableNames.add(var1 + "_" + var2);
			}
		}
		return variableNames;
	}
	
}
