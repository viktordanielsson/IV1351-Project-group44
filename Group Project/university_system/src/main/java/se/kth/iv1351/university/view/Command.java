
package se.kth.iv1351.university.view;

public enum Command {
    /**
     * Cost of course instance
     */
    COST,
    /**
     * add new teaching activity
     */
    ADD,
    /**
     *  modify the number of students
     */
    MODIFY,
    /**
     * allocate new teacher to teaching activity
     */
    ALLOCATE,
    /**
     * deallocate new teacher to teaching activity
     */
    DEALLOCATE,
    /**
     * L
     */
    DISPLAY,
    /**
     * Lists all commands.
     */
    HELP,
    /**
     * Leave the chat application.
     */
    QUIT,
    /**
     * None of the valid commands above was specified.
     */
    ILLEGAL_COMMAND
}