package se.kth.iv1351.university.view;

import se.kth.iv1351.university.controller.Controller;
import se.kth.iv1351.university.model.CostDTO;
import se.kth.iv1351.university.model.EmployeeAllocationDTO;

import java.util.Scanner;

public class BlockingInterpreter{
    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private Controller ctrl;
    private boolean keepReceivingCmds = false;

    /**
     * Creates a new instance that will use the specified controller for all operations.
     * 
     * @param ctrl The controller used by this instance.
     */
    public BlockingInterpreter(Controller ctrl) {
        this.ctrl = ctrl;
    }

    /**
     * Stops the commend interpreter.
     */
    public void stop() {
        keepReceivingCmds = false;
    }

    /**
     * Interprets and performs user commands. This method will not return until the
     * UI has been stopped. The UI is stopped either when the user gives the
     * "quit" command, or when the method <code>stop()</code> is called.
     */
    public void handleCmds() {
        keepReceivingCmds = true;
        while (keepReceivingCmds) {
            try {
                CmdLine cmdLine = new CmdLine(readNextLine());
                switch (cmdLine.getCmd()) {
                    case HELP:
                        for (Command command : Command.values()) {
                            if (command == Command.ILLEGAL_COMMAND) {
                                continue;
                            }
                            System.out.println(command.toString().toLowerCase());
                        }
                        break;
                    case QUIT:
                        keepReceivingCmds = false;
                        break;
                    case COST:                      // YEAR                                PERIOD                           COURSECODE
                        String[] userInputs = {cmdLine.getParameter(0), cmdLine.getParameter(1), cmdLine.getParameter(2)};
                        CostDTO cost = ctrl.calculateTeachingCost(userInputs[0],userInputs[1],userInputs[2]);
                        StringBuilder sb = new StringBuilder();
                        sb.append("Course Code\t Course Instance\tPeriod\tplanned cost\t actual cost\n");
                        sb.append(userInputs[2]+"\t\t "+
                        cost.getCourseInstanceId()+"\t\t"+
                        userInputs[1]+"\t");  

                        sb.append(cost.getPlannedCost()+ "\t "+cost.getActualCost());
                        System.out.println(sb);
                        break;

                    case ADD:                               //ACTIVITYNAME                                  FACTOR
                        ctrl.addNewTeachingActivity(cmdLine.getParameter(0), Double.parseDouble(cmdLine.getParameter(1)));
  
                        break;
                    case MODIFY:                                          // STUDY YEAR                , STUDYPERIOD,                 COURSECODE,                            MODIFYAMOUNT
                        ctrl.modifyCourseInstanceStudentNumber(cmdLine.getParameter(0),cmdLine.getParameter(1),cmdLine.getParameter(2), Integer.parseInt(cmdLine.getParameter(3)));

                        break;
                    case ALLOCATE:                                    // STUDY YEAR                 STUDYPERIOD                 COURSECODE                      ACTIVITYNAME                  EMPLOYMENTID                             ALLOCATED HOURS             
                        ctrl.allocatedTeacherToCourseInstance(cmdLine.getParameter(0),cmdLine.getParameter(1),cmdLine.getParameter(2),cmdLine.getParameter(3),cmdLine.getParameter(4),Double.parseDouble(cmdLine.getParameter(5)));
                        break;

                    case DEALLOCATE:                                  // STUDY YEAR                STUDYPERIOD                      COURSECODE                   ACTIVITYNAME                   EMPLOYMENTID                                 
                        ctrl.deallocateTeacherToCourseActivity(cmdLine.getParameter(0),cmdLine.getParameter(1),cmdLine.getParameter(2),cmdLine.getParameter(3),cmdLine.getParameter(4));
                        break; 
                    case DISPLAY: 
                                                // STUDY YEAR                   STUDYPERIOD                             COURSECODE                 ACTIVITYNAME                         EMPLOYMENTID          
                        String[] userInput = {cmdLine.getParameter(0), cmdLine.getParameter(1), cmdLine.getParameter(2), cmdLine.getParameter(3), cmdLine.getParameter(4)};
                        EmployeeAllocationDTO DTO = ctrl.displayTeacherAllocation(userInput[0],userInput[1],userInput[2],userInput[3],userInput[4]);
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Course Code\t Course Instance\tTeacher's name \t\t"+userInput[3]+" Hours\n");
                        stringBuilder.append(userInput[2]+"\t\t "+
                        DTO.getActivity().getCourseInstanceId()+"\t\t"+DTO.getEmployee().getName()+"\t "+
                        DTO.getHoursAllocated()+"\t");  
                        System.out.println(stringBuilder);                        
                        break;
                    default:
                        System.out.println("illegal command");
                }
            } catch (Exception e) {
                System.out.println("Operation failed");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String readNextLine() {
        System.out.print(PROMPT);
        return console.nextLine();
    }
}
