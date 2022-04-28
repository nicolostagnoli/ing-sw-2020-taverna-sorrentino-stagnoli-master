package it.polimi.ingsw.client.gui;

import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GodsPopup implements Initializable {

    public AnchorPane pane;
    private int numPlayers;

    public CheckBox apolloCheck, hephaestusCheck, artemisCheck, demeterCheck, atlasCheck, athenaCheck, minotaurCheck, panCheck, prometheusCheck;
    public CheckBox zeusCheck, tritonCheck, limusCheck, hestiaCheck, heraCheck;
    public ImageView okBtn;
    private final List<CheckBox> boxes = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        boxes.add(apolloCheck);
        boxes.add(hephaestusCheck);
        boxes.add(artemisCheck);
        boxes.add(demeterCheck);
        boxes.add(atlasCheck);
        boxes.add(athenaCheck);
        boxes.add(minotaurCheck);
        boxes.add(panCheck);
        boxes.add(prometheusCheck);
        boxes.add(zeusCheck);
        boxes.add(tritonCheck);
        boxes.add(limusCheck);
        boxes.add(hestiaCheck);
        boxes.add(heraCheck);
        okBtn.setVisible(false);
    }

    /**
     * To set the number of choice the user has to make
     * @param num
     */
    public void setNumPlayers(int num){
        this.numPlayers = num;
    }

    /**
     * To get the choices of the user
     * @return A list containing the names of the gods chosen
     */
    public List<String> getChoices(){

        List<String> gods = new ArrayList<>();

        for (CheckBox box : boxes) {
            if (box.isSelected())
                gods.add(box.getText().trim());
        }

        return gods;
    }

    private int countChecked(){
        int count = 0;
        for (CheckBox box : boxes){
            if (box.isSelected())
                count++;
        }
        return count;
    }

    private void enableAll(){
        for (CheckBox box : boxes){
            box.setVisible(true);
        }
    }

    /**
     * Called when a checkbox is clicked
     * @param mouseEvent
     */
    public void checkBoxClicked(MouseEvent mouseEvent) {
        if (countChecked() == numPlayers){
            for (CheckBox box : boxes){
                if (!box.isSelected())
                    box.setVisible(false);
            }
            okBtn.setVisible(true);
        } else {
            enableAll();
            okBtn.setVisible(false);
        }
    }

    /**
     * Called when the confirm button is clicked
     * @param mouseEvent
     */
    public void confirm(MouseEvent mouseEvent) {
        ((Stage)pane.getScene().getWindow()).close();
    }
}
