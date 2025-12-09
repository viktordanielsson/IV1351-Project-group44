package se.kth.iv1351.university.startup;

import se.kth.iv1351.university.view.BlockingInterpreter;

import se.kth.iv1351.university.controller.*;

public class Main 
{
    public static void main(String[] args) {
        new BlockingInterpreter(new Controller()).handleCmds();
    }
}
