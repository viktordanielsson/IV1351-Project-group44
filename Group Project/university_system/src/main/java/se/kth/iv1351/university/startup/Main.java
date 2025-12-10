package se.kth.iv1351.university.startup;

import se.kth.iv1351.university.view.BlockingInterpreter;

import se.kth.iv1351.university.controller.*;
import se.kth.iv1351.university.integration.UniversityDBException;

public class Main 
{
    public static void main(String[] args) {
        try {
            new BlockingInterpreter(new Controller()).handleCmds();
        } catch (UniversityDBException udbe) {
            System.out.print("Complete system failure");
            udbe.printStackTrace();
        }
    }
}
