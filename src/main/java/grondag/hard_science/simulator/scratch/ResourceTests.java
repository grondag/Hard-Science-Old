////apackage grondag.hard_science.simulator.scratch;
//
//import static org.junit.Assert.*;
//
//import org.junit.Test;
//
//import grondag.hard_science.gui.control.TabBar;
//import grondag.hard_science.simulator.machine.IResourceStack;
//
//public class ResourceTests
//{
//
//    @Test
//    public void test()
//    {
//        // SCENARIO: SEARCH INVENTORY - CLIENT 
//        
//        // on client, will give search a session identifier that is passed back and forth to server to match incoming updates
//        LiveStorageSearch<StorageTypeStack> search = ClientStorageIndexProxy.beginLiveItemSearch("iron");
//        
//        //search must implement List<IResourceStack<>> to be used to display in tab bar
//        TabBar<IResourceStack<StorageTypeStack>> bar = new TabBar(search);
//        
//        
//        search.setSearchString("gold");
//        
//        
//        //server needs to close open session if player is no longer looking at the screen, in case this gets missed
//        // see player.openContainer
//        search.close();
//        
//        
//        // SCENARIO: SEARCH INVENTORY - SERVER
//        
//        // top-level calls will mirror client
//        LiveStorageSearch<StorageTypeStack> search = ServerStorageIndexProxy.beginLiveItemSearch("iron");
//            // called by above
//            StorageIndexProxy.addListenter(search);
//            
//        search.setSearchString("gold");
//        
//        search.close();
//            StorageIndexProxy.removeListenter(search);
//        
//        // assumes stacks can't be negative, called by StorageIndex when it gets updates
//        LiveStorageSearch.notifyOfChange(stack, isRemoved); 
//        
//        // called by individual storages
//        search.notifyOfChange(stack, isRemoved);
//        StorageIndexProxy.register(IStorage);
//        StorageIndexProxy.register(IStorage); // has to be called if goes offline
//        
//        
//        // SCENARIO: DISPLAY MACHINE STATUS - CLIENT 
//        LiveMachineStatus machineStatus = clientMachine.requestLiveStatus();
//        
//        // list resources buffered in the machine: materials, power, fluids, etc.
//        List<IResourceStack<?>> bufferedInputs = machineStatus.bufferedInputs();
//        List<IResourceStack<?>> bufferedOutputs = machineStatus.bufferedOutputs();
//        Job activeJob = machineStatus.activeJob();
//        RequestStatus status = activeJob.getStatus();
//        List<IResourceStack<?>> expectedInputs = activeJob.expectedInputs();
//        List<IResourceStack<?>> expectedOutputs = activeJob.expectedOutputs();
//        List<IResourceStack<?>> actualInputs = activeJob.actualInputs();
//        List<IResourceStack<?>> actualOutputs = activeJob.actualOutputs();
//        
//        
//        // SCENARIO: DISPLAY MACHINE STATUS - SERVER 
//        LiveMachineStatus machineStatus = serverMachine.requestLiveStatus();
//            serverMachine.addListener(machineStatus);
//        
//        // called in server machine on listeners when there is a change
//        machineStatus.checkForUpdates(this);
//        
//        
//        // SCENARIO: AUTOMATED BUILDING
//        
//        // user submits request to build a structure
//        BuildingPlan bPlan = myAdHocBuildTracker.createPlan();
//            List<BlockPlacement> placements = myAdHocBuildTracker.sequenceBlockPlacements();
//            
//            generateTasksForAllPlacements(List<BlockPlacement> placements)
//            {
//                for()
//            }
//            blockPlacement currentNode =
//            bPlan.addTopLevelTasks
//        myRecentBuildPlans.add(bPlan)
//        myAdHocBuildTracker.clear();
//        BuildingJob bJob = BuildingManager.createJob(bPlan);
//        
//                        
//        /**
//          
//          blocks require an queue selection agent"
//           useful when units of work have to pass through multiple processing steps with dependencies
//           that aren't part of the processing.  Block building is a good example.
//           We can make the blocks and load up drones, but blocks have to be placed on other blocks.
//           And the other blocks available to be placed on may change dynamically.
//           
//           Tasks in the queue are selected by the agent based on their ability to be consumed in the end.
//           In this case, agent checks to see if the block can be placed against a block already
//           in the world, or against a block that it has already promoted to a later step of processing.
//           
//           example
//           
//           placed 10 blocks
//           blocks 0-3 can be placed on the ground, or on each other
//           blocks 4-7 must be placed on one of the blocks 0-3, or on each other
//           blocks 8-9 must be placed on 4 and 7
//           
//           processing has three steps: forming, loading (into drone), and placing (moving drone and placing blocks)
//           
//           agent will immediately release blocks 0-3 for forming.
//           As those complete, they move the loading queue and agent will begin to release 4-7 for forming.
//           Drones can pick up as many blocks as they can from the loading queue
//           and then move to build site and start placing them.  Some risk of waiting if drones
//           get delayed or place blocks at different speeds, but probably okay.
//           
//           TaskContainer - container with tasks to be executed
//               scheduler - logic that determines which waiting tasks can be started
//               taskFactory - adapter that prepares inputs for execution by this container
//               limit - max number of items can be in queue before it stops accepting new items
//               inputContainer - container from which to pull work
//               takeCompleteTasks() - take completed work that can be drawn from this container
//               startReadyTask() - find a ready task, mark it started and return it
//               abortStartedTask(task) - return a started task to ready state
//               completeStartedTask(task) - move a started task to completed state
//               addCompletionListener() - will notify listeners when a task is completed
//               addReadyListener() - will notify listeners when a task is ready
//               
//               tasks in a container can be unready, ready, started or complete
//
//               taskContainers that use the same worker type will register
//               with a gobla index service that subscribes to addReadyListener()
//               so that workers can find tasks without querying individual jobs
//               
//               a machine or process that consumes the type of task in the container will try to claim ready tasks
//                   it will mark the task a started and then mark it complete when finished
//               only complete tasks can be taken by a downstream container
//               
//               container has a periodic upkeep loop
//                   if this.count < limit
//                       for input : inputContainer.takeCompletedTasks()
//                       {
//                           this.addTask(taskFactory.createTaskFromInput(input)
//                       }
//                   scheduler.schedule(this)       
//               
//           StartContainer - container with no inputs or scheduler
//               all tasks in a StartContainer have a complete status
//               typically no size limit
//                   
//           FinishContainer - container at the end of a job
//               taskFactory simply discards tasks and increments counters
//               no WIP limit
//               no scheduler
//               pulls completed tasks from upstream containers
//               and monitors job completion progress
//               
//           A construction job would look like this
//           
//           Schedulers
//               site scheduler - readies blocks that can be placed at construction site
//               fab scheduler - readies any blocks that can be placed on blocks that have completed fabrication 
//               load scheduler - readies any blocks that can be placed on blocks that have been loaded by a drone
//               
//           
//           FabricationContainer 
//               has no inputContainer
//               has all of the job's generic blockplacement tasks in unready status
//               compound scheduler (site or fab)
//               listeners: fab scheduler, fabrication manager
//               
//               the fabrication machines will manage their own logistics for building
//               the blocks.  The global fab index service provides a feasibilty and estimation function
//               for use by the construction planner.
//               
//           TransportContainer
//               FabricationContainer is input
//               factory simply creates a transport task to move completed blocks to the drone loading site
//               scheduler readies all jobs with no exclusion
//               tasks are marked complete when they arrive at loading site
//               transport subsystem handles sub-planning, execution and estimation of transport tasks
//               listeners: transport manager
//               
//            LoadContainer
//                TransportContainer is input
//                construction drones complete tasks by picking up blocks
//                compound scheduler: (site or load)
//                listeners: load scheduler, construction drone manager
//                
//            BuildContainer
//                LoadContainer is input
//                drones use this to report completion but manage the placement directly
//                no listeners, only used to report status
//                no scheduler, all tasks begin with started status
//                
//                
//            -----
//            
//            
//            so let's look at the processing subsystems that consume the tasks in each of these containers
//            
//            
//            FabricationManager
//                execute tasks that consume material inputs and process them into material outputs
//                also consumes power, computation and may consume transport resources
//                different FabManager for each type of fabrication (nano-fab, crushing, block forming, etc.)
//                Machines that perform multiple functions can be used by more than one FabricationManager
//                computes a preference score for each machine based on priorities
//                for tasks in queue, or being estimated, generates procurement orders for necessary inputs
//                may cancel or change procurement orders if a better way to fabricate something becomes available
//                when all material inputs are reserved, appended to processing queue (may be different priority buckets)
//                added to queue of machine with highest preference score
//                outputs may remain reserved for next step regardless of storage location
//                
//            ProcessingMachine
//                knows how to do one or more processing types
//                provides estimates of costs and outputs
//                scans queue for job tickets
//                expects ticket to have a reservation for all inputs
//                may maintain a buffer quantity of frequently used resources (power, resin, water, etc.)
//                issues transport requests for reserved material inputs when it can receive them
//                produces outputs, puts in output buffer, notifies caller of completion
//                if outputs are not removed from output buffer before it fills, may redirect them to storage  
//                
//            FabricationTask
//                specific to a fabrication type
//                task parameters (modelState, substance type, for example)
//                execute
//                getInputs 
//                procureInputs - called by FabManager when task is being readied to processing, reserves inputs
//                setInputReservations - called 
//                addListener - so that task can notify queue when inputs have been procured and is ready to process
//                getInputReservations - called by machine when it picks up task and is ready to receive inputs
//                setStatus - called by machine / manager to update status
//                setOututReservation - called by machine when reserved outputs are placed in buffer or storage
//                
//                
//            QUESTION: if an input is not in storage and must be fabricated to complete a fabrication task...
//                who issues the request to fabricate the input? FabManager, FabTask, ProcurmentManager?
//                Has to be Procurement, right? Because FabTask didn't know it wasn't available until
//                it asked the procurement manager for the material.
//                
//            StorageManagerImpl - what is stored and where?
//                issues transport requests to put things in storage - where should it be stored?
//                
//            ProcurementManager - who gets to use what is stored?
//                queues prioritized resource requests and apportions them 
//                reserves stored material for a particular task or purpose
//                relies on inventory manager to know what is stored and where
//                reservations aren't locale/lot specific for same material/itemstack 
//                    as example: machine outputs may not go to next task in job if equivalent outputs are closer
//                issues fabrication requests for materials that aren't available (not in storage or already reserved)
//                          
//         */
//        // user can request job status via mechanism similar to machines, see above
//        
//        
//        // forces work break down and may generate multiple possible plans
//        // removes any plans that have unmeetable external dependencies (inputs or conditions that don't come from a task within the plan)
//        // this includes availability of raw materials, production capacity, drone capacity, etc
//        // Then selects the best available plan according to current strategic stance set by user
//        //  Tradeoffs in stance are by function (building, defense, etc.) and choices are speed, efficiency (cost), quality (may not apply for all)
//        // Then makes best plan the active plan.  Or sets no active plan if there are no feasible plans.
//        // Can be called on a job that is already active.  
//        // Doing so will consider only current resources and incomplete tasks 
//        bJob.plan();
//        
//        
//        /**
//        / sample optimization problems
//         * 
//         * several tasks can be completed on two different machines
//         * for all tasks, one of the machines will be most efficient at task level
//         * if all tasks use the single machine, there will be delay overall
//         * would only accept this delay if efficiency is top priority
//         * 
//         * handle this by deciding at last possible moment
//         * each production task has a single provider queue that will route work dynamically based on priorties
//         * plan WBS only needs to know if the task is *possible* and have an estimate (based on recent history) of production cost
//         * plan will be refined at the task level as work gets done
//         * 
//         * another example, a job needs iron and there are four possible ways to get iron
//         * 1) use existing on-hand
//         * 2) conventional ore mining / processing
//         * 
//         * Above illustrates key point for fungible resources - iron, water, power, etc that may have multiple
//         * different forms (blocks/ingots, tanks/on-demand/containerized,  joules/fuel).
//         * All forms of a fungible resource are treated as a single dependency/task for dependent tasks.  
//         * The actual production method will be dynamically determined and may vary within the same job.
//         * 
//         * Fungible resources are only counted as fungible if a 2-way conversion method exists.
//         * (It may have its own dependencies on machines, power, etc.)
//         * The coversion of the fungible resources will have its own sub-plan that is dynamically managed.
//         * 
//         * Producible resources have at least one automated production method that consumes time, machine capacity and/or other resources.
//         * A non-producible resource has to be obtained by the player. 
//         * If a plan requires non-producible resource not in inventory, the plan is not viable.
//         * 
//         * Reclaimable resources can be broken down into other resources that are ingredients in the reclaimable resource.
//         * The output of reclaimation are never included in plans to avoid introducing cycles.  
//         * Instead, if a reclaimable resource has a minimum stocking unit level and the level in storage
//         * drops below this, the excess will be automatically reclaimed.
//         * 
//         * For example, suppose salt and water make brine, and brine can be converted back to solt and water.
//         * Suppose a process needs both water and brine as inputs.  It will use brine on hand, and it will make
//         * brine if some is needed, but it will not produce water from brine as part of the job.  It will
//         * instead rely on reclamation to reclaim excess brine as a separate management function.
//         * 
//         * If more than one, non-fungible resource can be used to produce something, and one of the resources
//         * is non-producible and has other uses, the planner may consume the producible resource, even if the non-producible resource is on hand.
//         * This should probably be a strategy setting, or an inventory setting for the item itself, like minimum stocking level.
//         * 
//        */
//        
//         // To plan our construction job, we work backwards:
//          
//         // The construction task requires a drone and a block, both at a certain location.
//         Plan newPlan = new Plan(bJob.jobTask);
//         
//         // ... in plan constructor
//         newPlan.topLevelTask = jobTask;
//         jobTask.planSubtasks();
//         
//         // construction task will generate these subtasks...
//         mySubtasks.add(new DroneTask())
//         
//
//        // determine loosely sequenced production plan for optimal construction
//        
//        // determine resource availability and sub-production plan
//        
//        // submit production requests to queue, with dependences
//        
//        
//        // submit construction requests to queue, with dependencies on production requests
//        // and, if appropriate, other construction requests
//    }
//
//}
