import java.util.*;
import java.io.*;

class ProcessUnprocessSingleFile extends Thread {

    private String fileName;
    private Boolean status;
    private Map<String,String> mapOfUpdatedWords;

    public ProcessUnprocessSingleFile(String fileName,Boolean status,Map<String,String> mapOfUpdatedWords) {
        this.fileName = fileName;
        this.status = status;
        this.mapOfUpdatedWords = mapOfUpdatedWords;
    } 
    public void run() {

        String destinationDirectry = null;
        String sourceDirectory = null;
        if(!status) {
            destinationDirectry = new String("./Processed/");
            sourceDirectory = new String("./Unprocessed/");
        }   
        else {
            destinationDirectry = new String("./Reprocessed/");
            sourceDirectory = new String("./Processed/");
        }
        File sourcefile = new File(sourceDirectory+fileName);
        File destFile = new File(destinationDirectry+fileName);
        Scanner fileReader = null;
        FileWriter fileWriter = null;

        try {
            if(!destFile.exists()) {
                destFile.createNewFile();
            }
            fileReader = new Scanner(sourcefile);
            PrintWriter destwriter = new PrintWriter(destFile); 
            
            if(fileReader != null && destwriter!= null)  {
                fileReader.useDelimiter("\n");
                for(int i=0;fileReader.hasNext();i++) {

                    String line = fileReader.next();
                    for(String key : mapOfUpdatedWords.keySet()) {
                        if(line.contains(key)) {
                            line = line.replaceAll(key,mapOfUpdatedWords.get(key));
                        }
                    }
                    destwriter.println(line);
                }
            }
            fileReader.close();
            destwriter.close();
        } catch (FileNotFoundException e) {
            System.out.println(fileName+" Not Found In Directory "+sourceDirectory+"!!");
            return;
        } catch (IOException e) {
            System.out.println("Error In Writing Data To File !!");
            return;
        }
    }    
}

class FileProcessorUnprocessor extends Thread {

    private List<String> fileNames;
    private Map<String,Boolean> fileStatus;
    private Map<String,String> mapOfUpdatedWords;
    Boolean processor;
    private FileProcessorUnprocessor() {
        //Initially value for processor is true but in another constructor, respective changed value will be updated
        this.processor = true ;
        this.fileNames = new ArrayList<String>();
        this.fileStatus = new HashMap<String,Boolean>();
        this.mapOfUpdatedWords = new HashMap<String,String>();
    }
    public FileProcessorUnprocessor(Boolean processor) {
        this();
        this.processor = processor;
    }

    //This method will read words from excel which are needed to be updated and will add in the mapOfUpdatedWords.
    //Taking here .csv file and not excel as excel i.e. .xls or .xlsx need API to add in classpath and get thirdparty jars, so considering CSV file.
    private void readDataFromExcelOrCSV() {
        String excelOrCSVPath = new String("./demo.csv");
        try {
            Scanner scanner = new Scanner(new File(excelOrCSVPath));
            scanner.useDelimiter("\n");
            while(scanner.hasNext()) {
                String splitted[] = scanner.next().split(",");
                mapOfUpdatedWords.put(splitted[0],splitted[1]);
            }
            scanner.close();
        } catch(FileNotFoundException exp) {
            System.out.println("No CSV or Excel File Found In The Resources Folder, Aborting The Program");
            System.exit(1);
        }
    }   

    //Get no. of files and filenames by reading directoryinfo.
    //If path contains 'Unprocessed' set status to false else if path contains 'Processed' set status to true
    private void getDirectoryInfo(Boolean processed) {

        String path = null;
        if(!processed)
            path = new String("./Unprocessed/");
        else
            path = new String("./Processed/");
        
        //following code in if can further be optimised
        if(path != null) {
            //Status will be false for file if path contains Unprocessed and that specific directory unprocessed or processed will be opened. 
            if(path.contains("Unprocessed")) {
                File directory = new File(path);
                if(directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    for(File file : files) {
                        fileNames.add(file.getName());
                        fileStatus.put(file.getName(),false);
                    }
                }
            }
            else {
                File directory = new File(path);
                if(directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    for(File file : files) {
                        fileNames.add(file.getName());
                        fileStatus.put(file.getName(),true);
                    }
                    Map<String,String> alterUpdationWords = new HashMap<String,String>();
                    for(String key : mapOfUpdatedWords.keySet()) {
                        alterUpdationWords.put(mapOfUpdatedWords.get(key),key);
                    }
                    this.mapOfUpdatedWords = alterUpdationWords;     
                }
            }
        }
    }

    private void processUnprocess() {
        //This will be Thread-0 or Thread-1 which will give birth to all the sub threads each thread for single file. 
        for(String fileName : fileNames) {
            ProcessUnprocessSingleFile processingFile = new ProcessUnprocessSingleFile(fileName,fileStatus.get(fileName),mapOfUpdatedWords);
            processingFile.start();
            if(fileStatus.get(fileName) == true)
                System.out.println("Reprocessing File : "+fileName+" ....");
            else 
                System.out.println("Processing File : "+fileName+" ....");
        }
    }

    public void process() {
        
        readDataFromExcelOrCSV();
        //For processing the file giving false.
        getDirectoryInfo(false);
        processUnprocess();
    }
    
    public void unprocess() {
       
        readDataFromExcelOrCSV();
        //For unprocessing the file giving false.
        getDirectoryInfo(true);
        processUnprocess();
    }

    public void run() {
        if(processor) 
            process();
        else
            unprocess();
    }
}

class Main {
    public static void main(String[] args) {
        
        FileProcessorUnprocessor fileProcessor = new FileProcessorUnprocessor(true);   
        System.out.println("Processing Starts..............................................");   
        fileProcessor.start();
        try {
            //Stopping main thread till processing is done.
            fileProcessor.join();
            System.out.println("Processing Done !!!!!!!!!!!!!!!!!!!!!!!");
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
            
        FileProcessorUnprocessor fileUnProcessor = new FileProcessorUnprocessor(false);      
        System.out.println("\nReprocessing Starts..............................................");
        fileUnProcessor.start();
        try {
            //Stopping main thread till unprocessing is done.
            fileUnProcessor.join();
            System.out.println("Reprocessing Done !!!!!!!!!!!!!!!!!!!!!!!");
        }catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}