package br.unicamp.ic.zooexp.client;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import br.unicamp.ic.zooexp.core.client.Client;
import br.unicamp.ic.zooexp.core.client.ServerException;

public class ClientGui extends JFrame {

    /**
     * 
     */
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

		if (Character.isDigit(c)) {
		    buffer.append(c);
		}
	    }
	    super.insertString(offs, buffer.toString(), a);
	}

    }

    JButton setBt, readBt, addBt, subBt;
    JTextField writeField, readField;
    Client client;

    public ClientGui() throws UnknownHostException, IOException {
	super("Client");

	//Set up GUI
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
	setBt.addActionListener(new ActionListener(){

	    @Override
	    public void actionPerformed(ActionEvent e) {
		try {
		    client.set(getTextFieldValue());
		} catch (ServerException e1) {
		    // TODO Auto-generated catch block
		    
		} catch (IOException e1) {
		    // TODO Auto-generated catch block
		    
		}
		
	    }
	    
	});
	
	addBt.addActionListener(new ActionListener(){

	    @Override
	    public void actionPerformed(ActionEvent e) {
		try {
		    client.add(getTextFieldValue());
		} catch (ServerException e1) {
		    // TODO Auto-generated catch block
		    
		} catch (IOException e1) {
		    // TODO Auto-generated catch block
		    
		}

	    }
	    
	});
	
	
	
	subBt.addActionListener(new ActionListener(){

	    @Override
	    public void actionPerformed(ActionEvent e) {
		try {
		    client.sub(getTextFieldValue());
		} catch (ServerException e1) {
		    // TODO Auto-generated catch block
		    
		} catch (IOException e1) {
		    // TODO Auto-generated catch block
		    
		}

	    }
	    
	});
	
	readBt.addActionListener(new ActionListener(){

	    @Override
	    public void actionPerformed(ActionEvent e) {
		try {
		    int result = client.get();
		    readField.setText(Integer.toString(result));
		} catch (ServerException e1) {
		    // TODO Auto-generated catch block
		    
		} catch (IOException e1) {
		    // TODO Auto-generated catch block
		    
		}

	    }
	    
	});
	
	addWindowListener(new WindowAdapter(){

	    @Override
	    public void windowClosing(WindowEvent e) {
		client.disconnect();
	    }
	    
	});
	
	//Connect to server
	client = new Client();
	client.connect();
    }
    
    
    
    private int getTextFieldValue(){
	int result = 0;
	try{
	    result = Integer.parseInt(writeField.getText());
	}catch(NumberFormatException e){
	    // do nothing
	}
	return result;
    }

    /**
     * @param args
     * @throws IOException 
     * @throws UnknownHostException 
     */
    public static void main(String[] args) throws UnknownHostException, IOException {
	ClientGui client = new ClientGui();
	client.setVisible(true);
	client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

}
