package org.applied_geodesy.jag3d.ui.textfield;

import javafx.scene.control.TextField;

public class LimitedTextField extends TextField {

    private final int limit;

    public LimitedTextField(int limit) {
        this(limit, null);
    }
    
    public LimitedTextField(int limit, String text) {
    	super(text);
        this.limit = limit;
    }

    @Override
    public void replaceText(int start, int end, String text) {
        super.replaceText(start, end, text);
        this.verify();
    }

    @Override
    public void replaceSelection(String text) {
        super.replaceSelection(text);
        this.verify();
    }

    private void verify() {
        if (getText().length() > this.limit) {
            setText(getText().substring(0, this.limit));
            positionCaret(this.limit);
        }
    }
}