import javax.swing.*;

class GUI extends JFrame {
    public TitlesFields titlesFields = new TitlesFields(5);

    GUI() {
        add(titlesFields);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }
}
