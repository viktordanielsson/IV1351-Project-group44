package se.kth.iv1351.university.view;

import se.kth.iv1351.university.controller.Controller;
import java.util.Scanner;

public class BlockingInterpreter {
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
                    case COST:
                        System.out.println(ctrl.calculateTeachingCost(cmdLine.getParameter(0),cmdLine.getParameter(1),cmdLine.getParameter(2)));
                        /*ctrl.createAccount(cmdLine.getParameter(0));*/
                        break;
                    case DELETE:
                                /*ctrl.deleteAccount(cmdLine.getParameter(0)); */   
                    break;
                    case LIST:
                        /* List<? extends AccountDTO> accounts = null;
                        if (cmdLine.getParameter(0).equals("")) {
                            accounts = ctrl.getAllAccounts();
                        } else {
                            accounts = ctrl.getAccountsForHolder(cmdLine.getParameter(0));
                        }
                        for (AccountDTO account : accounts) {
                            System.out.println("acct no: " + account.getAccountNo() + ", "
                                            + "holder: " + account.getHolderName() + ", "
                                            + "balance: " + account.getBalance());
                        } */
                        break;
                    case MODIFY:
                        System.out.println(ctrl.modifyCourseInstanceStudentNumber("2025","P1","IS1200",1000));
                        /* 
                        ctrl.deposit(cmdLine.getParameter(0), 
                        Integer.parseInt(cmdLine.getParameter(1))); 
                        */
                        break;
                    case ALLOCATE:
                        ctrl.allocatedTeacherToCourseInstance("2025","P1","IS1200","EMP10001","Admin", 100.1);
                        /* 
                        ctrl.withdraw(cmdLine.getParameter(0), 
                        Integer.parseInt(cmdLine.getParameter(1))); 
                        */
                        break;
                    case DEALLOCATE:
                        /*
                        AccountDTO acct = ctrl.getAccount(cmdLine.getParameter(0));
                        if (acct != null) {
                            System.out.println(acct.getBalance());
                        } else {
                            System.out.println("No such account");
                        }
                        */
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
