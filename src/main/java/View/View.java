package View;

import Controller.Controller;

import java.util.TreeMap;

public class View implements IView {


    private Controller controller;

    public View(Controller controller){
        this.controller = controller;
    }


    @Override
    public void showDictionary(TreeMap dictionary) {

        //show it and stuff
    }
}
