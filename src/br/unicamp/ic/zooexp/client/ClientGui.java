package br.unicamp.ic.zooexp.client;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class ClientGui extends JFrame implements
	DispatcherThread.ClientEventListener {

    private static final long serialVersionUID = -3859715597744512663L;

    // To make JTextField restrict its input only to numbers we
    // must reimplement the document it uses
    private static class UnsignedIntegerDocument extends PlainDocument {

	private static final long serialVersionUID = 1L;

	@Override
	public void insertString(int offs, String str, AttributeSet a)
		throws BadLocationException {
	    StringBuffer buffer = new StringBuffer();
	    for (int i = 0; i < str.length(); i++) {
		
		char c = str.charAt(i);
		//allows digits and  a minus sign if it is on the start
		if (Character.isDigit(c) || (offs == 0 && c == '-')) {
		    buffer.append(c);
		}
	    }
	    super.insertString(offs, buffer.toString(), a);
	}

    }

    JButton setBt, readBt, addBt, subBt;
    JTextField writeField, readField;
    DispatcherThread dispatcher;

    public ClientGui() {
	super("Client");

	// Set up GUI
	setBt = new JButton("SET");
	readBt = new JButton("READ");
	addBt = new JButton("ADD");
	subBt = new JButton("SUB");
	writeField = new JTextField();
	writeField.setText("0");
	writeField.setDocument(new UnsignedIntegerDocument());
	readField = new JTextField();
	readField.setText("0");
	readField.setDocument(new UnsignedIntegerDocument());
	readField.setEditable(false);

	Container container = getContentPane();
	container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
	container.add(writeField);

	Box box1 = Box.createHorizontalBox();
	box1.add(setBt);
	box1.add(addBt);
	box1.add(subBt);
	container.add(box1);

	container.add(Box.createVerticalStrut(5));

	Box box2 = Box.createHorizontalBox();
	box2.add(readBt);
	box2.add(readField);
	container.add(box2);
	pack();
	this.setResizable(false);
	this.setName("Client");

	// Setup listeners
	setBt.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		dispatcher.setValue(getTextFieldValue());
	    }

	});

	addBt.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		dispatcher.addValue(getTextFieldValue());
	    }

	});

	subBt.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		dispatcher.subValue(getTextFieldValue());
	    }

	});

	readBt.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		dispatcher.readValue();
	    }

	});

	addWindowListener(new WindowAdapter() {

	    @Override
	    public void windowClosing(WindowEvent e) {
		dispatcher.disconnect();
	    }

	});

	//Start dispatcher thread
	dispatcher = new DispatcherThread(this);
	dispatcher.start();
    }

    int getTextFieldValue() {
	int result = 0;
	try {
	    result = Integer.parseInt(writeField.getText());
	} catch (NumberFormatException e) {
	    // do nothing
	}
	return result;
    }

    @Override
    public void onClientError(final Exception e) {
	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		JOptionPane.showMessageDialog(ClientGui.this, e
			.getMessage(), "Some error occurred",
			JOptionPane.ERROR_MESSAGE);

	    }

	});

    }

    @Override
    public void onResult(final int value) {
	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		readField.setText(Integer.toString(value));

	    }

	});
    }

    public static void main(String[] args) {
	ClientGui client = new ClientGui();
	client.setVisible(true);
	client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

}
