package data;

import java.awt.SystemTray;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.Attributes.Name;

import javax.activation.DataSource;
import javax.sound.sampled.Line;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

public class DataProcess {
	public int fileCount = 0;
	public int importCount = 0;
	private int maxImport = 0;
	private String rootKey;
	private Vector<ArrayList<String>> nameVector;
	private HashMap<String, Character> founderHashMap;
	
	private HashMap<String, Vector<String>> graph;
	
	private Vector<Article> articles;
	
	public DataProcess(){
		this.graph = new HashMap<String, Vector<String>>();
		this.setNameVector(new Vector<ArrayList<String>>());
		articles =  new Vector<Article>();
		
		//to init the founder
		founderHashMap = new HashMap<String, Character>();
		founderHashMap.put("Henk Bodrogi", null);
		founderHashMap.put("Carmine Osvaldo", null);
		founderHashMap.put("Ale L. Hanne", null);
		founderHashMap.put("Jeroen Karel", null);
		founderHashMap.put("Valentine Mies", null);
		founderHashMap.put("Yanick Cato", null);
		founderHashMap.put("Joreto Katell", null);
	}
	
	public HashMap<String, Vector<String>> getGraph(){
		return graph;
	}
	
	public void setGraph(HashMap<String, Vector<String>> graph){
		this.graph = graph;
	}
	
	public String getRootKey() {
		return rootKey;
	}

	public void setRootKey(String rootKey) {
		this.rootKey = rootKey;
	}
	
	public static void main(String[] args){
		DataProcess dataProcess = new DataProcess();
		
		dataProcess.extractDateAndName("/Users/apple/Downloads/MC1 Data/MC1 Data/articles");
		Vector<ArrayList<String>> vector = dataProcess.getNameVector();
//		for (int i = 0; i < vector.size(); i++) {
//			ArrayList<String> names = vector.get(i);
//			System.out.print("article " + i + ": ");
//			for(int j = 0; j < names.size(); ++j){
//				System.out.print(names.get(j) + ",");
//			}
//			System.out.print("\n");
//		}
		
		int size = dataProcess.getArticles().size();
		for(int i = 0; i < size; ++i){
			if(!dataProcess.getArticles().get(i).getIsHighLight())
				continue;
			
			System.out.println("article " + i + ": " );
			System.out.println("\t date: " + dataProcess.getArticles().get(i).getDate());
			int personSize = dataProcess.getArticles().get(i).getPersons().size();
			
			System.out.print("\t person: ");
			for(int person = 0; person < personSize; ++person){
				System.out.print(dataProcess.getArticles().get(i).getPersons().get(person));
				if(person != personSize - 1){
					System.out.print(",");
				}
				else
					System.out.print("\n");
			}
		}
	}
	
	public void traverseOneDirectory(String pathName){
		File dir = new File(pathName);
		File[] files = null;
		if(dir.isDirectory()){
			//System.out.println("input is a dir.");
			files = dir.listFiles();
			for(int i = 0; i < files.length; ++i){
				traverseOneDirectory(files[i].getPath());
			}
		}
		else if(dir != null){
			if(dir.getPath().endsWith(".java")){
				fileCount++;
				countImport(pathName);
			}
		}
	}
	
	public void countImport(String pathname){
		File file = new File(pathname);
		BufferedReader reader = null;
		String importStringObj = "import";
		try{
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			String className = file.getName().substring(0, file.getName().length() - 5);
			
			int importCount = 0;
			while((line = reader.readLine()) != null){
				//System.out.println(line);
				String[] splits = line.split(" ");
				if(splits.length > 0 && splits[0].equals(importStringObj)){
					Vector<String> values = graph.get(className);
					String[] prefixs = splits[splits.length - 1].split("\\.");
					String importClassName = new String();
					if(prefixs.length > 0)
						importClassName = prefixs[prefixs.length - 1].substring(0, prefixs[prefixs.length - 1].length() - 1);
					else
						importClassName = splits[splits.length - 1].substring(0, splits[splits.length - 1].length() - 1);
					
					if(importClassName.equals("*")){
						//System.out.println("find *");
						continue;
					}
						
					importCount++;
					if(values == null){
						Vector<String> value = new Vector<String>();
						value.add(importClassName);
						graph.put(className, value);
					}
					else{
						graph.get(className).add(importClassName);
					}
				}
			}
			if(importCount > maxImport){
				maxImport = importCount;
				rootKey = className;
			}
			//System.out.println(importCount);
		}catch(IOException e){
			e.printStackTrace();
		}	
	}
	
	public void generateNetwork(){
		Iterator<Entry<String, Vector<String>>> iterator = graph.entrySet().iterator();
		String key = new String();
		
		HashMap<String, Integer> visitedMap = new HashMap<String, Integer>();
		Queue<String> queue = new LinkedList<String>();
		int capacity = (int)(graph.size()/0.75 + 1);
		HashMap<String, Vector<String>> updatedGraph = new HashMap<String, Vector<String>>(capacity);
		
		//bfs to create a network
		Vector<String> value = null;
//		Vector<String> value = graph.get(rootKey);
//		int length = value.size();
//		for(int i = 0; i < length; ++i){
//			if(!queue.offer(value.get(i)))
//				System.out.println("offer ele to the queue failed.");
//		}
//		visitedMap.put(rootKey, 1);
//		updatedGraph.put(rootKey, graph.get(rootKey));
		
		if(!queue.offer(rootKey))
			System.out.println("offer ele to the queue failed.");
		
		
		while(!queue.isEmpty()){
			key = queue.poll();
			
			//visit the node here
			if(visitedMap.get(key) == null){
				visitedMap.put(key, 1);
				updatedGraph.put(key, new Vector<String>());
				//Vector<String> newValue =  new Vector<String>();
				value = graph.get(key);
				if(value != null){
					for(int i = 0; i < value.size(); ++i){
						if(!visitedMap.containsKey(value.get(i))){
							updatedGraph.get(key).add(value.get(i));
							if(!queue.offer(value.get(i)))
								System.out.println("offer ele to the queue failed.");
						}
					}
				}
			}
		}
		
		setGraph(updatedGraph);
	}
	
	public String formatNetwork(String fileName){
		String res = new String();
		String key = new String();
		Vector<String> value = null;
		int rootSize = 200;
		int defaultSize = 30;
		int sizeStep = -40;
		int capacity = (int)(graph.size()/0.75 + 1);
		HashMap<String, ConvertToJson> jsonMap = new HashMap<String, ConvertToJson>(capacity);
		HashMap<String, Integer> visitedMap = new HashMap<String, Integer>(capacity);

		//System.out.println("root value size: " + graph.get(rootKey).size());
		
		//still use bfs, cause it will get the right size relationship
		Queue<String> queue = new LinkedList<String>();
		if(!queue.offer(rootKey))
			System.out.println("offer ele to the queue failed.");
		
		while(!queue.isEmpty()){
			key = queue.poll();
			
			ConvertToJson json = null;
			ConvertToJson child = null;
			if(!visitedMap.containsKey(key)){
				visitedMap.put(key, 1);
				if(!jsonMap.containsKey(key)){
					json = new ConvertToJson();
					json.setName(key);
					if(key.equals(rootKey))
						json.setSize(rootSize);
					else {
						json.setSize(defaultSize);
					}
					jsonMap.put(key, json);
				}
				else
					json = jsonMap.get(key);
				
				value = graph.get(key);
				
				if(value.size() > 0)
					json.setChildren(new ArrayList<ConvertToJson>());
				
				for(int i = 0; i < value.size(); ++i){
					if(!visitedMap.containsKey(value.get(i))){
						if(!queue.offer(value.get(i)))
							System.out.println("offer ele to the queue failed.");
						
						if(!jsonMap.containsKey(value.get(i))){
							child = new ConvertToJson();
							child.setName(value.get(i));
							jsonMap.put(value.get(i), child);
						}
						else
							child = jsonMap.get(value.get(i));
						
						if(graph.get(child.getName()).size() > 0)
							child.setChildren(new ArrayList<ConvertToJson>());
						
						child.setSize(json.getSize() + sizeStep);
						if(child.getSize() < defaultSize)
							child.setSize(defaultSize);
						json.getChildren().add(child);
					}
				}
				
			}
		}
		
		Iterator<Entry<String, ConvertToJson>> iterator = jsonMap.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, ConvertToJson> entry = iterator.next();
			if(entry.getValue().getChildren() == null)
				entry.getValue().setSize(defaultSize);
		}
		
//		while(iterator.hasNext()){
//			Entry<String, Vector<String>> entry = iterator.next();
//		Iterator<Entry<String, Vector<String>>> iterator = graph.entrySet().iterator();
//		while(iterator.hasNext()){
//			Entry<String, Vector<String>> entry = iterator.next();
//			ConvertToJson json = new ConvertToJson();
//			json.setName(entry.getKey());
//			if(entry.getKey().equals(rootKey))
//				json.setSize(rootSize);
//			else {
//				json.setSize(defaultSize);
//			}
//			json.setChildren(new ArrayList<ConvertToJson>());
//			if(jsonMap.get(entry.getKey()) == null){
//				//System.out.println("add");
//				jsonMap.put(entry.getKey(), json);
//			}
//			else
//				json = jsonMap.get(entry.getKey());
//			
//			for(int i = 0; i < entry.getValue().size(); ++i){
//				ConvertToJson child = null;
//				if(!jsonMap.containsKey(entry.getValue().get(i))){
//					child = new ConvertToJson();
//					child.setName(entry.getValue().get(i));
//					child.setChildren(new ArrayList<ConvertToJson>());
//					jsonMap.put(child.getName(), child);
//				}
//				else{
//					child = jsonMap.get(entry.getValue().get(i));
//				}
//				
//				child.setSize(json.getSize() + sizeStep);
//				json.getChildren().add(jsonMap.get(entry.getValue().get(i)));
//			}
//		}

        ObjectMapper mapper = new ObjectMapper();
        
        mapper.setVisibility(JsonMethod.FIELD, Visibility.ANY);
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        
		try {
			//System.out.println("jsonMap size: " + jsonMap.size());
			if(jsonMap.get(rootKey) != null)
				res = mapper.writeValueAsString(jsonMap.get(rootKey));
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(res);
        
        try {
        		FileOutputStream out = new FileOutputStream(fileName);
			out.write(res.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	public void extractDateAndName(String pathName){
		File dirPath = new File(pathName);
		BufferedReader reader = null;
		File[] files = dirPath.listFiles();
		SimpleDateFormat enDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
		SimpleDateFormat numDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
		if(files != null){
			System.out.println("file count: " + files.length);
			nameVector.setSize(files.length);
			articles.setSize(files.length);
			for(File article : files){
				ArrayList<String> nameArrayList = new ArrayList<String>();
				Article currentArticle = new Article();
				String dateString = null;
				Boolean dateFlagBoolean = false;
				try {
					reader = new BufferedReader(new FileReader(article));
					String line = null;
					while((line = reader.readLine()) != null){
						//extract the date
						if(!dateFlagBoolean){
							try {
								Date date = enDateFormat.parse(line);
								if(date != null){
									dateString = numDateFormat.format(date);	
								}
							} catch (Exception e) {
								// TODO: handle exception
							}
							try {
								Date date = numDateFormat.parse(line);
								if(date != null){
									dateString = numDateFormat.format(date);	
								}
							} catch (Exception e) {
								// TODO: handle exception
							}
						}

						if(dateString != null){
							if(!dateFlagBoolean){
								//nameArrayList.add(0, dateString);
								currentArticle.setDate(dateString);
							}
							dateFlagBoolean = true;
						}
						
						//to be revised to get a better effect
						//e.g. use a stop list to avoid the media, or skip early lines
						line = line.replace('.', '\0');
						line = line.replace(',', '\0');
						String[] nameStrings = line.split(" ");
						if(nameStrings.length > 0){
							for(int i = 0; i < nameStrings.length; ++i){
								//when meet a word starting with a upper case, check the next word
								String word = nameStrings[i];
								if(word.length() > 3){
									//first char is HighCase
									char firstChar = word.charAt(0);
									char secondChar = word.charAt(1);
									if(firstChar >= 'A' && firstChar <= 'Z' && secondChar >= 'a' && secondChar <='z'){
										//boundary check
										if(i + 1 < nameStrings.length){
											String nextWordString = nameStrings[i + 1];
											if(nextWordString.length() > 1){
												firstChar = nextWordString.charAt(0);
												secondChar = nextWordString.charAt(1);
												if(firstChar >= 'A' && firstChar <= 'Z' && secondChar >= 'a' && secondChar <='z'){
													++i;
													word += " " + nextWordString;
													//now, just give a naive highlight test
													nameArrayList.add(word);
													if(founderHashMap.containsKey(word))
														currentArticle.setIsHighLight(true);
												}
											}
										}
									}
								}
							}
						}
					}
					reader.close();
					nameVector.set(Integer.parseInt(article.getName().split("\\.")[0]), nameArrayList);
					currentArticle.setPersons(nameArrayList);
					articles.set(Integer.parseInt(article.getName().split("\\.")[0]), currentArticle);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Vector<ArrayList<String>> getNameVector() {
		return nameVector;
	}

	public void setNameVector(Vector<ArrayList<String>> nameVector) {
		this.nameVector = nameVector;
	}

	public Vector<Article> getArticles() {
		return articles;
	}

	public void setArticles(Vector<Article> articles) {
		this.articles = articles;
	}
}
