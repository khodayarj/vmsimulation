package Models;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import javax.swing.JFrame;

import org.apache.commons.collections15.CollectionUtils;

/**
 * Created by I857455 on 1/18/2018.
 */
public class Cloud {

    List<PM> pmList;
    List<Assignment> currentAssignments;
    List<Assignment> newAssignments;
    List<Migration> migrations;
    List<Migration> nextPhaseMigrations;
    Report report;

    public List<Assignment> getCurrentAssignments() {
        return currentAssignments;
    }

    public void setCurrentAssignments(List<Assignment> currentAssignments) {
        this.currentAssignments = currentAssignments;
    }

    public Cloud(List<PM> pmList) {
        this.pmList = pmList;
    }

    public List<PM> getPmList() {
        return pmList;
    }

    public void setPmList(List<PM> pmList) {
        this.pmList = pmList;
    }

    public List<Assignment> getNewAssignments() {
        return newAssignments;
    }

    public void setNewAssignments(List<Assignment> newAssignments) {
        this.newAssignments = newAssignments;
    }

    public List<Migration> getMigrations() {
        return migrations;
    }

    public void setMigrations(List<Migration> migrations) {
        this.migrations = migrations;
    }

    public List<Migration> getNextPhaseMigrations() {
        return nextPhaseMigrations;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public void setNextPhaseMigrations(List<Migration> nextPhaseMigrations) {
        this.nextPhaseMigrations = nextPhaseMigrations;
    }

    public Cloud(List<PM> pmList, List<Assignment> currentAssignments) {
        this.pmList = pmList;
        this.currentAssignments = currentAssignments;
    }

    public Cloud() {
        this.pmList = new ArrayList<>();
        this.currentAssignments = new ArrayList<>();
        this.newAssignments = new ArrayList<>();
        this.nextPhaseMigrations = new ArrayList<>();
        this.report = new Report();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String timestamp = sdf.format(new Date());
        report.setTimeStampCalcStarted(timestamp);
    }


    public void displayCloudInfo() {
        System.out.println("Cloud info--------------");
        System.out.println("List of PMs :");
        pmList.forEach(pm -> {
            System.out.println(pm.getName() + " [memory:" + pm.getMemoryCapacity() + ",  CPU:"
                    + pm.getProcessorCapacity() + ", network:" + pm.getNetworkCapacity());
        });
        System.out.println("List of VMs :");
        currentAssignments.forEach(assignment -> {
            VM vm = assignment.getVm();
            System.out.println(vm.getName() + " [memory:" + vm.getMemorySize() + ",  CPU:"
                    + vm.getProcessorSize() + ", network:" + vm.getNetworkSize());
        });
        System.out.println("---------------------------------");
    }

    public Assignment findAssignment(List<Assignment> assignments, VM vm) {
        Assignment result;
        result = assignments.stream().filter(obj -> obj.getVm().equals(vm)).findFirst()
                .get();
        return result;
    }


    //change assignment of a vm to new location
    //no need to check here
    //in migration = true for migration process, remove the other assignment in current (initial one)
    //if (hasFreeCapacityFor(pm, vm)) {
    public void assignToCurrentLocation(VM vm, PM pm, boolean inMigration) throws Exception {

        if (!hasFreeCapacityFor(currentAssignments, pm, vm)) {
            throw new Exception(
                    "there is no free capacity for this assignment " + vm.getName() + " to " + pm.getName());
        }
        Assignment newAssignment = new Assignment(pm, vm);
        Predicate<Assignment> assignmentPredicate = p -> p.getVm() == vm;
        if (!inMigration) {
            currentAssignments.removeIf(assignmentPredicate);
        }
        currentAssignments.add(newAssignment);
    }


    public void assignToNewLocation(VM vm, PM pm) throws Exception {

        if (!hasFreeCapacityFor(newAssignments, pm, vm)) {
            throw new Exception(
                    "there is no free capacity for this assignment " + vm.getName() + " to " + pm.getName());
        }
        Assignment newAssignment = new Assignment(pm, vm);
        Predicate<Assignment> assignmentPredicate = p -> p.getVm() == vm;
        newAssignments.removeIf(assignmentPredicate);
        newAssignments.add(newAssignment);

    }

    public void removeFromCurrent(VM vm, PM pm) {
        final Assignment[] remove = new Assignment[1];
        currentAssignments.forEach(assignment -> {
            if (assignment.getVm().equals(vm) && assignment.getPm().equals(pm)) {
                remove[0] = assignment;
            }
        });

        currentAssignments.remove(remove[0]);
    }


    public List<VM> getVMs() {
        List<VM> vms = new ArrayList<>();
        currentAssignments.forEach(assignment -> {
            vms.add(assignment.getVm());
        });
        return vms;
    }


    public List<VM> getVMForPM(List<Assignment> assignments, PM pm) {
        List<VM> vms = new ArrayList<>();
        assignments.forEach(assignment -> {
            if (assignment.getPm().equals(pm)) {
                vms.add(assignment.getVm());
            }
        });
        return vms;
    }

    public void showAssignments(boolean end) {
        System.out.println("-------------------------current Assignments");
        pmList.forEach(pm -> {
            System.out.print(pm.getName() + " [free memory:" + freeMemory(currentAssignments, pm) + ", free CPU:"
                    + freeProcessor(currentAssignments, pm) + ",free network:" + freeNetwork(currentAssignments, pm)
                    + "]   assigned VMs: ");
            getVMForPM(currentAssignments, pm).forEach(vm -> {
                System.out.print(vm.getName() + " ");
            });
            System.out.println();
        });
        if (!end) {
            System.out.println("-------------------------New Assignments");
            pmList.forEach(pm -> {
                System.out.print(pm.getName() + " [free memory:" + freeMemory(newAssignments, pm) + ", free CPU:"
                        + freeProcessor(newAssignments, pm) + ",free network:" + freeNetwork(newAssignments, pm)
                        + "]   assigned VMs: ");
                getVMForPM(newAssignments, pm).forEach(vm -> {
                    System.out.print(vm.getName() + " ");
                });
                System.out.println();
            });
        }
    }


    public PM getLegacyVMLocation(VM vm) {
        final PM[] pm = new PM[1];
        this.getCurrentAssignments().forEach(assignment -> {
            if (assignment.getVm() == vm) {
                pm[0] = assignment.getPm();
            }
        });
        return pm[0];
    }


    public List<Migration> generateMigrations() {
        List<Migration> migrations = new ArrayList<>();
        currentAssignments.forEach(assignment -> {
            if (!getLegacyVMLocation(assignment.getVm())
                    .equals(findAssignment(newAssignments, assignment.getVm()).getPm())) {
                migrations.add(new Migration(assignment.getPm(),
                        findAssignment(newAssignments, assignment.getVm()).getPm(), assignment.getVm()));
            }
        });
        this.migrations = migrations;
        return migrations;
    }

    //outgoing set of pm , separated based on each destination pm
    public List<VMSet> getOutgoingVmSetsFrom(List<Migration> migrations, PM pm) {

        Map<PM, List<VM>> migrationsOfPM = new HashMap<>();
        migrations.forEach(migration -> {
            if (migration.getSource().equals(pm)) {
                if (migrationsOfPM.get(migration.getDestination()) == null) {
                    List<VM> newList = new ArrayList<>();
                    newList.add(migration.getVm());
                    migrationsOfPM.put(migration.getDestination(), newList);
                } else {
                    migrationsOfPM.get(migration.getDestination()).add(migration.getVm());
                }
            }
        });

        List<VMSet> vmSets = new ArrayList<>();
        for (Map.Entry<PM, List<VM>> entry : migrationsOfPM.entrySet()) {
            VMSet vmSet = new VMSet();
            vmSet.setVMList(entry.getValue());
            vmSets.add(vmSet);
        }

        return vmSets;

    }


    public VMSet getAllIncomingVMsTo(List<Migration> migrations, PM pm) {
        VMSet vmSet = new VMSet();
        migrations.forEach(migration -> {
            if (migration.getDestination().equals(pm)) {
                vmSet.add(migration.getVm());
            }
        });
        return vmSet;
    }

    public VMSet getSetOfAllMigratingVMsFrom(List<Migration> migrations, PM pm) {
        VMSet vmSet = new VMSet();
        migrations.forEach(migration -> {
            if (migration.getSource().equals(pm)) {
                vmSet.add(migration.getVm());
            }
        });
        return vmSet;
    }


    //get outgoing vmset from pm to pm
    public VMSet getVMGoingFromTo(List<Migration> migrations, PM source, PM destination) {
        VMSet vmSet = new VMSet();
        migrations.forEach(migration -> {
            if (migration.getSource().equals(source) && migration.getDestination().equals(destination)) {
                vmSet.add(migration.getVm());
            }
        });
        return vmSet;
    }


    //all the sets  , no use for now
    public List<VMSet> getAllOutGoingSets(List<Migration> migrations) {
        List<VMSet> vmSetList = new ArrayList<>();
        this.getPmList().forEach(pm -> {
            if (!getOutgoingVmSetsFrom(migrations, pm).isEmpty()) {
                getOutgoingVmSetsFrom(migrations, pm).forEach(vmsetlist -> {
                    vmSetList.add(vmsetlist);
                });
            }
        });
        return vmSetList;
    }


    public int freeMemory(List<Assignment> assignments, PM pm) {
        final int[] freeMemory = {pm.getMemoryCapacity()};
        assignments.forEach(assignment -> {
            if (assignment.getPm().equals(pm)) {
                freeMemory[0] -= assignment.getVm().getMemorySize();
            }
        });

        return freeMemory[0];

    }

    public int freeProcessor(List<Assignment> assignments, PM pm) {
        final int[] freeMemory = {pm.getProcessorCapacity()};
        assignments.forEach(assignment -> {
            if (assignment.getPm().equals(pm)) {
                freeMemory[0] -= assignment.getVm().getProcessorSize();
            }
        });

        return freeMemory[0];

    }

    public int freeNetwork(List<Assignment> assignments, PM pm) {
        final int[] freeMemory = {pm.getNetworkCapacity()};
        assignments.forEach(assignment -> {
            if (assignment.getPm().equals(pm)) {
                freeMemory[0] -= assignment.getVm().getNetworkSize();
            }
        });

        return freeMemory[0];

    }

    public boolean hasFreeCapacityFor(List<Assignment> assignments, PM pm, VM vm) {
        return freeMemory(assignments, pm) >= vm.getMemorySize() && freeNetwork(assignments, pm) >= vm.getNetworkSize()
                && freeProcessor(assignments, pm) >= vm.getProcessorSize();
    }


    public String getFreeCapacity(List<Assignment> assignments, PM pm) {
        return "free memory:" + freeMemory(assignments, pm) + " / free CPU" + freeProcessor(assignments, pm);
    }

    public boolean hasFreeCapacityFor(List<Assignment> assignments, PM pm, VMSet vmSet) {
        int neededMemory = 0, neededCpu = 0, neededNet = 0;

        for (int i = 0; i < vmSet.getVMList().size(); i++) {
            VM vm = vmSet.getVMList().get(i);
            neededMemory += vm.getMemorySize();
            neededCpu += vm.getProcessorSize();
            neededNet += vm.getNetworkSize();
        }

        return freeMemory(assignments, pm) >= neededMemory && freeNetwork(assignments, pm) >= neededNet
                && freeProcessor(assignments, pm) >= neededCpu;
    }


    public List<Migration> getMigrationsFrom(List<Migration> migrations, PM source) {
        List<Migration> migrationList = new ArrayList<>();
        migrations.forEach(m -> {
            if (m.getSource().equals(source)) {
                migrationList.add(m);
            }
        });
        return migrationList;
    }


    //check of logic is not deteriorated
    //done : it's each pm must be sets
    //I made it private so not to be used in code
    public DependencyGraph generateDependencyGraph(List<Migration> migrationList) {

        // return generateOnoueDependencyGraph(migrationList);

        DependencyGraph dependencyGraph = new DependencyGraph();
        pmList.forEach(sourcePm -> {
            getOutgoingVmSetsFrom(migrationList, sourcePm).forEach(vmSet -> {
                PM destination = findMigrationOfVM(vmSet.getVMList().get(0), migrationList).getDestination();
                if (!hasFreeCapacityFor(currentAssignments, destination, vmSet)) {

                    //adding individual vm set
                    getOutgoingVmSetsFrom(migrationList, destination).forEach(destOutSet -> {
                        dependencyGraph.addDependent(vmSet, destOutSet);
                    });

                    PM source = findMigrationOfVM(vmSet.getVMList().get(0), migrationList).getSource();
                    ComplexDependency complexDependency = new ComplexDependency(vmSet, source, destination);
                    dependencyGraph.getCmplxDepend().add(complexDependency);
                }
            });
        });

        return dependencyGraph;
    }

    //from a vm set that is going to a pm to all vms that are leaving that pm
    public DependencyGraph generateOnoueDependencyGraph(List<Migration> migrationList) {
        DependencyGraph dependencyGraph = new DependencyGraph();

        pmList.forEach(destinationPM -> {
            VMSet depTo = getSetOfAllMigratingVMsFrom(migrationList, destinationPM);
            VMSet allComing = getAllIncomingVMsTo(migrationList, destinationPM);
            VMSet excess = getExcessVMSet(allComing, destinationPM);
            if (!excess.getVMList().isEmpty()) {
                dependencyGraph.addDependent(excess, depTo);

                //complex dependency won't work here ,
                //it needs from one pm to one pm dependency

            }
        });

        return dependencyGraph;
    }


    //get incoming VMs that can not be migrated to
    private VMSet getExcessVMSet(VMSet allComing, PM destinationPM) {
        List<Assignment> copyOfAssignments = new ArrayList<Assignment>(currentAssignments);

        VMSet vmSet = new VMSet();
        List<VM> allComingVMs = allComing.getVMList();
        Collections.sort(allComingVMs, new Comparator<VM>() {
            @Override
            public int compare(VM o1, VM o2) {
                return o2.getMemorySize() - o1.getMemorySize();
            }
        });

        allComingVMs.forEach(vm -> {
            if (hasFreeCapacityFor(copyOfAssignments, destinationPM, vm)) {
                Assignment assignment = new Assignment(destinationPM, vm);
                copyOfAssignments.add(assignment);
            } else {
                vmSet.add(vm);
            }
        });
        return vmSet;
    }

    public void setMigrationTimes(List<Migration> migrations) {
        migrations.forEach(migration -> {
            migration.setWeight(migration.getVm().getMemorySize());
        });
    }


    public void setDependencyWeights(List<Migration> migrations) {
        DependencyGraph dependencyGraph = generateDependencyGraph(migrations);
        Map<VMSet, List<VMSet>> dependencyMap = dependencyGraph.getDependencyMap();

        List<VMSet> removedInEdge = new ArrayList<>();
        List<VMSet> allSets = getAllOutGoingSets(migrations);

        while (removedInEdge.size() < allSets.size()) {

            allSets.forEach(set -> {
                if (!removedInEdge.contains(set)) {
                    boolean hasInEdge = false;
                    for (Map.Entry<VMSet, List<VMSet>> entry : dependencyMap.entrySet()) {
                        if (entry.getValue().contains(set) && !removedInEdge.contains(entry.getKey())) {
                            hasInEdge = true;
                        }
                    }

                    if (!hasInEdge) {
                        int setWeight = maxWeightOfSet(set, migrations);
                        if (dependencyMap.get(set) != null) {
                            dependencyMap.get(set).forEach(dependentSet -> {
                                dependentSet.getVMList().forEach(vm -> {
                                            findMigrationOfVM(vm, migrations)
                                                    .setWeight(findMigrationOfVM(vm, migrations).getWeight() + setWeight);
                                        }
                                );
                            });
                        }
                        removedInEdge.add(set);
                    }
                }
            });
        }

    }


    //this is setting dependency weights before solving the cycles (just removing the
    public void setDependencyWeightsO(List<Migration> migrations) {
        DependencyGraph dg = generateOnoueDependencyGraph(migrations);
        Set<List<VMSet>> cycles = detectCyclesO(dg);

        //now we must detect and remove one edge for cycles
        final int[] index = {0};
        cycles.stream().forEach(vmSets -> {

            final VMSet[] minWeightSet = {vmSets.get(0)};
            vmSets.forEach(vmSet -> {
                //must be a key, left side of dependency
                if (dg.getDependencyMap().get(vmSet) != null && vmSet.getWeightSum() < minWeightSet[0].getWeightSum()) {
                    minWeightSet[0] = vmSet;
                }
            });
            System.out.println("cycle " + (++index[0]) + " --> " + minWeightSet[0]);

            //remove the dependency of the minWeight , remove it's outgoing edges
            dg.getDependencyMap().remove(minWeightSet[0]);
        });

        //now we have dg without cycles
        List<VM> node = getVMs();

        while (!node.isEmpty()) {
            List<VM> o = getVMsWihoutInEdge(node, dg);
            o.forEach(oVM -> {
                if (dg.getKeyContaining(oVM) != null) {
                    dg.getDependencyMap().get(dg.getKeyContaining(oVM)).get(0).getVMList().forEach(dependentVM -> {
                        Migration depMig = findMigrationOfVM(dependentVM, migrations);
                        depMig.setWeight(depMig.getWeight() + findMigrationOfVM(oVM, migrations).getWeight());

                    });
                }
            });

            for (VM vm : o) {
                if (dg.getKeyContaining(vm) != null) {
                    dg.getDependencyMap().remove(dg.getKeyContaining(vm));
                }

            }

            node.removeAll(o);
            o.clear();
        }


    }

    private List<VM> getVMsWihoutInEdge(List<VM> node, DependencyGraph dg) {
        List<VM> wie = new ArrayList<>();
        node.forEach(nodeVM -> {
            if (dg.getEntryContaining(nodeVM) == null) {
                wie.add(nodeVM);
            }
        });
        return wie;
    }


    public List<VM> getVMsWithoutOutEdges(DependencyGraph dg) {
        List<VM> woe = new ArrayList<>();
        getMigrations().forEach(migration -> {
            VM vm = migration.getVm();
            //remove the second ?
            if (dg.getKeyContaining(vm) == null) {
                woe.add(vm);
            }
        });
        return woe;
    }


    private int maxWeightOfSet(VMSet set, List<Migration> migrations) {
        final int[] weight = {0};
        set.getVMList().forEach(vm -> {
                    if (findMigrationOfVM(vm, migrations).getWeight() > weight[0]) {
                        weight[0] = findMigrationOfVM(vm, migrations).getWeight();

                    }
                }
        );

        return weight[0];
    }


    private int sumWeightOfSet(VMSet set, List<Migration> migrations) {
        final int[] weight = {0};
        set.getVMList().forEach(vm -> {
                    weight[0] += findMigrationOfVM(vm, migrations).getWeight();
                }
        );
        return weight[0];
    }


    public Migration findMigrationOfVM(VM vm, List<Migration> migrations) {
        final Migration[] migration = new Migration[1];
        migrations.forEach(mig -> {
            if (mig.getVm().equals(vm)) {
                migration[0] = mig;
            }
        });

        return migration[0];

    }

    public void showCycles(DependencyGraph dGraph) {

        Set<List<VMSet>> cycleSet = detectCycles(dGraph);

        if (cycleSet.isEmpty()) {
            System.out.println("There is no cycles");
        } else {
            System.out.println("There are cycles :");
        }

        cycleSet.forEach(newset -> {
            System.out.println(newset);
        });
    }


    public void solveCycles() {
        getAllOutGoingSets(migrations).forEach(vmSet -> {
            DependencyGraph dGraph = generateOnoueDependencyGraph(migrations);
            if (Collections.frequency(dGraph.getPath(vmSet, vmSet), vmSet) > 1) {
                System.out.println("There is a cycle :");
                System.out.println(dGraph.getPath(vmSet, vmSet));
                VMSet bestCandidate = findTheMinWeightSet(dGraph.getPath(vmSet, vmSet));
                PM bestPm = null;
                try {
                    bestPm = findBestTempPM(dGraph.getPath(vmSet, vmSet), vmSet);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.exit(0);
                }
                updateMigration(bestCandidate, bestPm);
            }

        });
    }


    // In Onoue, the path and cycles are based on the VMs not VM sets, because the cycles
    //are complex and not from set to the same set
    public void solveCyclesOn(Set<List<VMSet>> cycles, DependencyGraph dg) {

        final DependencyGraph[] d = {dg};
        Set<List<VMSet>> solved = new HashSet<>();
        final int[] index = {0};
        final int[] numberOfUnsolved = {0};
        cycles.stream().forEach(vmSets -> {
            final VMSet[] minWeightSet = {vmSets.get(0)};
            vmSets.forEach(vmSet -> {
                //must be a key, left side of dependency
                if (d[0].getDependencyMap().get(vmSet) != null && vmSet.getWeightSum() < minWeightSet[0]
                        .getWeightSum()) {
                    minWeightSet[0] = vmSet;
                }
            });
            PM tempLocation = null;
            try {
                tempLocation = findBestTempPM(vmSets, minWeightSet[0]);
                //as a test
                if (tempLocation != null) {
                    solved.add(vmSets);
                    updateMigration(minWeightSet[0], tempLocation);
                } else {
                    numberOfUnsolved[0]++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            d[0] = generateOnoueDependencyGraph(migrations);
        });
        if (numberOfUnsolved[0] > 0) {
            System.out.println(" Unsolved cycles - not finding temporary servers for " + numberOfUnsolved[0] + " cycles");
        }
        report.setNumberOfSolvedCycles(report.getNumberOfSolvedCycles() + solved.size());
        cycles.removeAll(solved);
    }


    public Set<List<VMSet>> detectCycles(DependencyGraph dGraph) {

        Set<List<VMSet>> allCyleSet = new HashSet<List<VMSet>>();
        getAllOutGoingSets(migrations).forEach(vmSet -> {
            if (Collections.frequency(dGraph.getPath(vmSet, vmSet), vmSet) > 1) {

                List<VMSet> cycle = dGraph.getPath(vmSet, vmSet);
                cycle.remove(cycle.size() - 1);
                Collections.sort(cycle, new Comparator<VMSet>() {
                    @Override
                    public int compare(VMSet o1, VMSet o2) {
                        return o1.getVMList().toString().compareTo(o2.getVMList().toString());
                    }
                });
                CollectionUtils.isEqualCollection(cycle, new ArrayList<>());

                allCyleSet.add(cycle);
            }

        });

        Set<List<VMSet>> cyleSet = new HashSet<List<VMSet>>();
        final boolean[] itThere = {false};
        allCyleSet.forEach(set -> {
            cyleSet.forEach(newset -> {
                if (CollectionUtils.isEqualCollection(set, newset)) {
                    itThere[0] = true;
                }
            });
            if (!itThere[0]) {
                cyleSet.add(set);
            }
        });

        return cyleSet;
    }


    public Set<List<VMSet>> detectCyclesO(DependencyGraph dGraph) {

        Set<List<VMSet>> allCyleSet = new HashSet<List<VMSet>>();

        getVMs().forEach(vm -> {

            List<VMSet> path = dGraph.getPathO(vm, vm);
            if (path.size() > 1) {
                List<VMSet> cycle = path;
                // cycle.remove(cycle.size() - 1);
//                Collections.sort(cycle, new Comparator<VMSet>() {
//                    @Override
//                    public int compare(VMSet o1, VMSet o2) {
//                        return o1.getVMList().toString().compareTo(o2.getVMList().toString());
//                    }
//                });
//                CollectionUtils.isEqualCollection(cycle , new ArrayList<>());

                allCyleSet.add(cycle);
            }

        });

        Set<List<VMSet>> cyleSet = new HashSet<List<VMSet>>();
        final boolean[] itThere = {false};
        allCyleSet.forEach(set -> {
            itThere[0] = false;
            cyleSet.forEach(newset -> {
                if (CollectionUtils.isEqualCollection(set, newset)) {
                    itThere[0] = true;
                }
            });
            if (!itThere[0]) {
                cyleSet.add(set);
            }

        });

        return cyleSet;
    }


    public void showCyclesO(DependencyGraph dGraph) {

        Set<List<VMSet>> cycleSet = detectCyclesO(dGraph);

        if (cycleSet.isEmpty()) {
            System.out.println("There is no cycles");
        } else {
            System.out.println("There are " + cycleSet.size() + "  cycles :");
        }

        final int[] index = {0};
        cycleSet.forEach(newset -> {
            System.out.println("--------------cycle number : " + (++index[0]));
            System.out.println(newset);
        });
    }


    private void updateMigration(VMSet bestCandidate, PM bestPm) {
        List<VM> vmList = bestCandidate.getVMList();
        vmList.forEach(vm -> {
            Migration oldmig = findMigrationOfVM(vm, migrations);
            Migration oldTemp = findMigrationOfVM(vm, nextPhaseMigrations);
            Migration newMig = null;
            if (oldmig != null) {
                newMig = new Migration(oldmig.getSource(), bestPm, vm);
                newMig.setWeight(oldmig.getWeight());
                migrations.remove(oldmig);
            }
            else if (oldTemp != null) {
                newMig = new Migration(oldTemp.getSource(), bestPm, vm);
                newMig.setWeight(oldTemp.getWeight());
                nextPhaseMigrations.remove(oldTemp);
            } else {
                try {
                    throw new Exception("can not find old migration for " + vm.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            System.out.println("new temp Migration :" + newMig);
            nextPhaseMigrations.remove(oldTemp);

            nextPhaseMigrations
                    .add(new Migration(bestPm, oldmig != null ? oldmig.getDestination() : oldTemp.getDestination(),
                            vm));
            // migrations.remove(oldmig);
            report.setNumberOFTempMig(report.getNumberOFTempMig() + 1);
            migrations.add(newMig);
        });
        System.out.println("--------------------------");
    }


    private VMSet findTheMinWeightSet(List<VMSet> vmSetList) {
        final VMSet[] found = {vmSetList.get(0)};
        vmSetList.forEach(vmSet -> {
            if (sumWeightOfSet(vmSet, migrations) < sumWeightOfSet(found[0], migrations)) {
                found[0] = vmSet;
            }
        });
        return found[0];
    }


    private PM findBestTempPM(List<VMSet> cycleVMSetList, VMSet candidate) throws Exception {
        final PM[] pm = new PM[1];
        List<PM> candidatePms = pmsNotInCycle(cycleVMSetList);

        candidatePms.forEach(candidatePm -> {
            if (hasFreeCapacityForSet(currentAssignments, candidatePm, candidate)) {
                pm[0] = candidatePm;
            }
        });
        if (pm[0] == null) {
            report.setNumberOfFailedAttempts(report.getNumberOfFailedAttempts() + 1);
        }
        return pm[0];

    }


    private boolean hasFreeCapacityForSet(List<Assignment> assignments, PM pm, VMSet vmSet) {
        final boolean[] hasCp = {true};
        vmSet.getVMList().forEach(vm -> {
            if (!hasFreeCapacityFor(assignments, pm, vm)) {
                hasCp[0] = false;
            }
        });
        return hasCp[0];
    }


    private List<PM> pmsNotInCycle(List<VMSet> cycleVMSetList) {
        List<PM> pms = new ArrayList<PM>(pmList);
        cycleVMSetList.forEach(vmSet -> {
            vmSet.getVMList().forEach(vm -> {
                migrations.forEach(migration -> {
                    if (migration.getVm().equals(vm)) {
                        pms.remove(migration.getDestination());
                    }
                });
            });
        });
        return pms;
    }


    public void drawComplexGraph(DependencyGraph dependencyGraph) {
        Graph<String, String> g = new SparseMultigraph<String, String>();
        dependencyGraph.getCmplxDepend().forEach(complexDependency -> {

            String firstNode = getSetOfAllMigratingVMsFrom(migrations, complexDependency.getSource()).toString();
            String secondNode = getSetOfAllMigratingVMsFrom(migrations, complexDependency.getDestination()).toString();
            g.addVertex(firstNode);
            g.addVertex(secondNode);

            g.addEdge(complexDependency.getVmSet().toString(), firstNode, secondNode, EdgeType.DIRECTED);
        });

        Layout<String, String> layout = new CircleLayout(g);
        layout.setSize(new Dimension(400, 400));
        BasicVisualizationServer<String, String> vv =
                new BasicVisualizationServer<String, String>(layout);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        vv.setPreferredSize(new Dimension(420, 420));

        JFrame frame = new JFrame("Simple Graph View");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
    }


    //draw a dependency graph based on the nodes as vm sets
    public void draw(DependencyGraph dependencyGraph) {
        Map<VMSet, List<VMSet>> dependencyMap = dependencyGraph.getDependencyMap();

        Graph<String, String> g = new SparseMultigraph<String, String>();

        for (Map.Entry<VMSet, List<VMSet>> entry : dependencyMap.entrySet()) {

            g.addVertex(entry.getKey().toString());
            entry.getValue().forEach(vmSet -> {
                g.addVertex(vmSet.toString());
            });

        }

        final int[] counter = {0};
        for (Map.Entry<VMSet, List<VMSet>> entry : dependencyMap.entrySet()) {
            entry.getValue().forEach(vmSet -> {
                g.addEdge(String.valueOf(counter[0]++), (entry.getKey().toString()), (vmSet.toString()),
                        EdgeType.DIRECTED);
            });

        }

        // g.addEdge("a1" , "a2" , "this" , EdgeType.DIRECTED);
        Layout<String, String> layout = new CircleLayout(g);
        layout.setSize(new Dimension(400, 400));
        BasicVisualizationServer<String, String> vv =
                new BasicVisualizationServer<String, String>(layout);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.setPreferredSize(new Dimension(420, 420));

        JFrame frame = new JFrame("Simple Graph View");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
    }


    public VMSet getCurrentVMSetsForPM(PM pm) {
        VMSet vmSet = new VMSet();
        this.getCurrentAssignments().forEach(assignment -> {
            if (assignment.getPm().equals(pm)) {
                vmSet.add(assignment.getVm());
            }
        });

        return vmSet;
    }

    public PM findPMByName(String s) {
        return pmList.stream().filter(thispm -> thispm.getName().equals(s)).findFirst().orElse(null);
    }


    //for migration process, where there are some infeasible VMs waiting for migration
    //but newly generated cycles won't let them
    public void removeVMsInCycle(Set<List<VMSet>> c, List<VM> l) {
        c.forEach(setList -> {
            setList.forEach(vmSet -> {
                l.removeAll(vmSet.getVMList());
            });
        });
    }

    public void printReport() {

        DependencyGraph d = generateOnoueDependencyGraph(getMigrations());
        Set<List<VMSet>> ct = detectCyclesO(d);
        report.setNumberOfCurrentCycles(ct.size());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String timestamp = sdf.format(new Date());
        report.setTimeStampFinished(timestamp);
        System.out.println(report);
    }

    public void writeReport() {

        try {
            Files.write(Paths.get("myfile.txt"), "the text".getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

    }

    public void removeDependantVMs(DependencyGraph d, List<VM> l) {
        List<VM> newL = new ArrayList<>();
        for (int i = 0 ; i<l.size() ; i++){
            if (d.getKeyContaining(l.get(i)) == null){
                newL.add(l.get(i));
            }
        }
        l =  newL;
    }

}
