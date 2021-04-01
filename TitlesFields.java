import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

//Probably useless

class TitlesFields extends JPanel {
    private ArrayList<JTextField> titleFields = new ArrayList<>();
    private JButton plusBtn = new JButton("+");
    private JButton minusBtn = new JButton("-");
    private ActionListener act = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {

        }
    };

    TitlesFields(int numberOfFields) {
        setLayout(new BorderLayout());
        JPanel leftSide = new JPanel();
        add(leftSide, BorderLayout.CENTER);
        leftSide.setLayout(new BoxLayout(leftSide, BoxLayout.Y_AXIS));
        for (int i=0; i<numberOfFields; i++) {
            JTextField tf = new JTextField();
            titleFields.add(tf);
            leftSide.add(tf);
            tf.setColumns(12);
        }

        JPanel rightSide = new JPanel();
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        add(rightSide, BorderLayout.EAST);

        rightSide.add(plusBtn);
        rightSide.add(minusBtn);





    }



}
