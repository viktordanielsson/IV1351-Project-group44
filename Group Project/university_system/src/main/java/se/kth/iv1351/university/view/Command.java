
package se.kth.iv1351.university.view;

public enum Command {
    /**
     * Creates a new account.
     */
    COST,
    /**
     * Lists all existing accounts.
     */
    LIST,
    /**
     * Deletes the specified account.
     */
    DELETE,
    /**
     * Deposits the specified amount to the specified account
     */
    MODIFY,
    /**
     * Withdraws the specified amount from the specified account
     */
    ALLOCATE,
    /**
     * Lists the balance of the specified account.
     */
    DEALLOCATE,
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