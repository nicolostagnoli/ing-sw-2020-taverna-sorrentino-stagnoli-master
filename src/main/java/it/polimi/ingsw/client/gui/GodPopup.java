package it.polimi.ingsw.client.gui;

import com.google.gson.Gson;
import it.polimi.ingsw.model.God;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class GodPopup implements Initializable {
    public ToggleGroup group;
    public ImageView okBtn;
    public RadioButton radioGod1, radioGod2, radioGod3;
    public ImageView godImg1, godImg2, godImg3;

    private List<ImageView> godsImages = new ArrayList<>();
    private List<RadioButton> buttons = new ArrayList<>();
    private List<God> godsDescription = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        godsImages.add(godImg1);
        godsImages.add(godImg2);
        godsImages.add(godImg3);
        godImg1.setVisible(false);
        godImg2.setVisible(false);
        godImg3.setVisible(false);
        buttons.add(radioGod1);
        buttons.add(radioGod2);
        buttons.add(radioGod3);
        radioGod1.setVisible(false);
        radioGod2.setVisible(false);
        radioGod3.setVisible(false);
        okBtn.setVisible(true);
    }

    /**
     * To set the list of available gods
     * @param availableGods The list of available gods
     */
    public void setGods(List<String> availableGods){
        InputStream inputStream = this.getClass()
                .getClassLoader().getResourceAsStream("gods");
        if (inputStream != null) {
            Scanner sc = new Scanner(inputStream);
            Gson gson = new Gson();
            while (sc.hasNext()) {
                godsDescription.add(gson.fromJson(sc.nextLine(), God.class));
            }
            sc.close();
            try {
                inputStream.close();
            }   catch (IOException ignored) { }
        }

        for (int i = 0; i < availableGods.size(); i++){
            godsImages.get(i).setImage(new Image("GodCard/" + availableGods.get(i) + ".png"));
            godsImages.get(i).setVisible(true);
            buttons.get(i).setVisible(true);
            String godName = availableGods.get(i);
            God god = godsDescription.stream().filter(g -> g.getName().equals(godName)).collect(Collectors.toList()).get(0);
            buttons.get(i).getTooltip().setText(god.getDescription());
            buttons.get(i).setText(availableGods.get(i));
        }
        radioGod1.setSelected(true);
    }

    /**
     * To get the choice of the user
     * @return the name of the chosen god
     */
    public String getChoice(){
        return ((RadioButton) group.getSelectedToggle()).getText().trim();
    }

    /**
     * Called when the confirm button is clicked
     * @param mouseEvent
     */
    public void confirm(MouseEvent mouseEvent) {
        ((Stage)okBtn.getScene().getWindow()).close();
    }
}
