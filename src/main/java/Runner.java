import Models.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class Runner {

    public static void main(String[] args) throws Exception {
        List<String> files = new ArrayList<>();

        try(FileWriter fw = new FileWriter("myfile.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
        {
           readFiles("D:\\google drive\\vm migration\\generator\\build_29-06-2018\\outputx" , files);
            files.forEach(file -> {
                //out.println(runTheFile(file));
                out.println(file.toString());
            });
            //more code
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        try(FileWriter fw = new FileWriter("report.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
        {
            files.forEach(file -> {
                //out.println(runTheFile(file));
                out.println(runTheFile(file));
            });
            //more code
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }


    }

    private static void readFiles(String folderPath , List<String> files) {

        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        for (File directory : listOfFiles) {

             if (directory.isDirectory()){
                files.add(directory.listFiles()[1].toString());
            }
        }

    }


    public static String runTheFile(String filePath){



        Cloud current = new Cloud();

        //read Charles's version
         CsvReader.readFile(current , filePath);

        //out_inst_100_CONS-20-80_50_90-95.csv
        //out_inst_100_CONS-20-80_50_80-85.csv
        //we can use this function to read the set up and current and new placements from setup.txt

        //  SetUp.readSetUp(current);
        //  alternative way to set up , create an optimal new assignment and a random current
        //DataGenerator.setUpCloud(5 , 20, 1, 80 , current);

        //end of setting up the network

        // current.displayCloudInfo();
        // current.showAssignments(false);

        DependencyGraph dependencyGraph;

       // System.out.println("Onoue dependency graph");
        dependencyGraph = current.generateOnoueDependencyGraph(current.generateMigrations());
        //  dependencyGraph.printDependency();

        //  current.showCyclesO(dependencyGraph);

//        current.getVMsWithoutOutEdges(dependencyGraph).forEach(vm->{
//            System.out.println(vm);
//        });

        //current.draw(dependencyGraph);

//        System.out.println("old dependency graph");
//        dependencyGraph = current.generateDependencyGraph(current.generateMigrations());
//        dependencyGraph.printDependency();

//        //setting default migration weights
        current.setMigrationTimes(current.getMigrations());
        current.setDependencyWeightsO(current.getMigrations());
       // System.out.println(current.getMigrations());

/*

        System.out.println("Number of Migrations :" + current.getMigrations().size());

        System.out.println("migration with initial weights :");
        Collections.sort(current.getMigrations(), new Comparator<Migration>() {
            @Override
            public int compare(Migration o1, Migration o2) {
                return o2.getWeight() - o1.getWeight();
            }
        });
        System.out.println(current.getMigrations());

        dependencyGraph.printDependency();
        //****  two option to draw the dependency graph, based on VMs and based on PMs

       current.draw(dependencyGraph);
      // current.drawComplexGraph(dependencyGraph);

       current.showCycles(dependencyGraph);


*/
        // current.draw(dependencyGraph);
 /*
        current.setMigrationTimes(current.getMigrations());
        System.out.println(current.getMigrations());
        current.setDependencyWeightsO(current.getMigrations());
        System.out.println(current.getMigrations());

      //  current.solveCycles();




        //recreate dependency graph after solving the deadlocks
     //  dependencyGraph = current.generateOnoueDependencyGraph(current.getMigrations());

       current.draw(dependencyGraph);



      //  current.setDependencyWeights(current.getMigrations());


      //  System.out.println("migration with dependency weights :");
      //  System.out.println(current.getMigrations());


        //dependencyGraph.printDependency();
*/


        //  System.out.println(current.getMigrations());
//        System.out.println("-------solving-----------");
//        current.solveCyclesOn(current.detectCyclesO(dependencyGraph) , dependencyGraph);
//
//        dependencyGraph = current.generateOnoueDependencyGraph(current.getMigrations());
//
//
//        System.out.println("------------independant migration-----");
//        current.getVMsWithoutOutEdges(dependencyGraph).forEach(vm->{
//            System.out.println(vm);
//        });
//
//        System.out.println("-------------------------------");
//        System.out.println(current.getMigrations());
//
//        System.out.println("-------------------------------");
        System.out.println("-------------------------------process-------------"+ filePath);
        MigrationProcess migrationProcess = new MigrationProcess();
        migrationProcess.setPipelineDegree(1000);
        migrationProcess.setLinkDegree(1000);
        migrationProcess.setCloud(current);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String timestamp = sdf.format(new Date());
        current.getReport().setTimeStampMigStart(timestamp);

        try {
            migrationProcess.doMigrationsOnoue(dependencyGraph);
        } catch (Exception e) {
            e.printStackTrace();
        }

        current.showAssignments(true);

       return current.getReport().toString();


    }

}
