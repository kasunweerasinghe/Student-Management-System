package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import db.DBConnection;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import model.StudentDTO;
import view.tdm.StudentTM;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class StudentFormController {
    public AnchorPane StudentManageFormContext;
    public JFXTextField txtStudentID;
    public JFXTextField txtStudentName;
    public JFXTextField txtEmail;
    public JFXTextField txtContact;
    public JFXTextField txtAddress;
    public JFXTextField txtNIc;
    public JFXButton btnAddNewStudent;
    public JFXButton btnSave;
    public JFXButton btnDelete;
    public TableView<StudentTM> tblStudent;
    public Label lblDate;
    public Label lblTime;

    public void initialize() {
        loadDateAndTime();
        tblStudent.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudent.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblStudent.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("email"));
        tblStudent.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("contact"));
        tblStudent.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("address"));
        tblStudent.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("nic"));

        initUI();

        tblStudent.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnDelete.setDisable(newValue == null);
            btnSave.setText(newValue != null ? "Update" : "Save");
            btnSave.setDisable(newValue == null);

            if (newValue != null) {
                txtStudentID.setText(newValue.getId());
                txtStudentName.setText(newValue.getName());
                txtEmail.setText(newValue.getEmail());
                txtContact.setText(newValue.getContact());
                txtAddress.setText(newValue.getAddress());
                txtNIc.setText(newValue.getNic());

                txtStudentID.setDisable(false);
                txtStudentName.setDisable(false);
                txtEmail.setDisable(false);
                txtContact.setDisable(false);
                txtAddress.setDisable(false);
                txtNIc.setDisable(false);
            }
        });

        txtNIc.setOnAction(event -> btnSave.fire());
        loadAllStudents();

    }

    private void loadAllStudents() {
        tblStudent.getItems().clear();
        //Get all Students
        try {
            Connection connection = DBConnection.getDbConnection().getConnection();
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM ijse.Student");

            while (rst.next()) {
                //tblStudent.getItems().add(new StudentTM(rst.getString("id"), rst.getString("name"), rst.getString("email"),rst.getString("contact"),rst.getString("address"),rst.getString("nic")));
                tblStudent.getItems().add(new StudentTM(rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4), rst.getString(5), rst.getString(6)));
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        } catch (ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }


    }

    public void btnAddNewStudentOnAction(ActionEvent actionEvent) {
        txtStudentID.setDisable(false);
        txtStudentName.setDisable(false);
        txtEmail.setDisable(false);
        txtContact.setDisable(false);
        txtAddress.setDisable(false);
        txtNIc.setDisable(false);

        txtStudentID.clear();
        txtStudentID.setText(generateNewId());

        txtStudentName.clear();
        txtEmail.clear();
        txtContact.clear();
        txtAddress.clear();
        txtNIc.clear();
        txtStudentName.requestFocus();

        btnSave.setDisable(false);
        btnSave.setText("Save");
        tblStudent.getSelectionModel().clearSelection();
    }


    public void btnDeleteOnAction(ActionEvent actionEvent) {
        //Delete Student
        String id = tblStudent.getSelectionModel().getSelectedItem().getId();
        try {
            if (!existStudent(id)) {
                new Alert(Alert.AlertType.ERROR, "There is no such Student associated with the id " + id).show();
            }
            Connection connection = DBConnection.getDbConnection().getConnection();
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM ijse.Student WHERE studentId=?");
            pstm.setString(1, id);
            pstm.executeUpdate();

            tblStudent.getItems().remove(tblStudent.getSelectionModel().getSelectedItem());
            tblStudent.getSelectionModel().clearSelection();
            initUI();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to delete the Student " + id).show();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    boolean existStudent(String id) throws SQLException, ClassNotFoundException {
        Connection connection = DBConnection.getDbConnection().getConnection();
        PreparedStatement pstm = connection.prepareStatement("SELECT studentId FROM ijse.Student WHERE studentId=?");
        pstm.setString(1, id);
        return pstm.executeQuery().next();
    }

    public void btnSaveOnAction(ActionEvent actionEvent) {
        String id = txtStudentID.getText();
        String name = txtStudentName.getText();
        String email = txtEmail.getText();
        String contact = txtContact.getText();
        String address = txtAddress.getText();
        String nic = txtNIc.getText();


        if (btnSave.getText().equalsIgnoreCase("save")) {
            //Save Student
            try {
                if (existStudent(id)) {
                    new Alert(Alert.AlertType.ERROR, id + " already exists").show();
                }
                Connection connection = DBConnection.getDbConnection().getConnection();
                PreparedStatement pstm = connection.prepareStatement("INSERT INTO Student (studentId,studentName, email,contact,address,nic) VALUES (?,?,?,?,?,?)");
                pstm.setString(1, id);
                pstm.setString(2, name);
                pstm.setString(3, email);
                pstm.setString(4, contact);
                pstm.setString(5, address);
                pstm.setString(6, nic);
                pstm.executeUpdate();

                tblStudent.getItems().add(new StudentTM(id, name, email, contact, address, nic));
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Failed to save the student " + e.getMessage()).show();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else {
            //Update Student
            try {
                if (!existStudent(id)) {
                    new Alert(Alert.AlertType.ERROR, "There is no such Student associated with the id " + id).show();
                }
                Connection connection = DBConnection.getDbConnection().getConnection();
                PreparedStatement pstm = connection.prepareStatement("UPDATE Student SET studentName=?, email=?, contact=?, address=?, nic=? WHERE studentId=?");
                pstm.setString(1, name);
                pstm.setString(2, email);
                pstm.setString(3, contact);
                pstm.setString(4, address);
                pstm.setString(5, nic);
                pstm.setString(6, id);
                pstm.executeUpdate();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Failed to update the student " + id + e.getMessage()).show();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            StudentTM selectedStudent = tblStudent.getSelectionModel().getSelectedItem();
            selectedStudent.setName(name);
            selectedStudent.setEmail(email);
            selectedStudent.setContact(contact);
            selectedStudent.setAddress(address);
            selectedStudent.setNic(nic);
            tblStudent.refresh();
        }

        btnAddNewStudent.fire();
    }

    private void initUI() {
        txtStudentID.clear();
        txtStudentName.clear();
        txtEmail.clear();
        txtContact.clear();
        txtAddress.clear();
        txtNIc.clear();

        txtStudentID.setDisable(true);
        txtStudentName.setDisable(true);
        txtEmail.setDisable(true);
        txtContact.setDisable(true);
        txtAddress.setDisable(true);
        txtNIc.setDisable(true);

        txtStudentID.setEditable(false);
        btnSave.setDisable(true);
        btnDelete.setDisable(true);
    }

    private String generateNewId() {
        try {
            Connection connection = DBConnection.getDbConnection().getConnection();
            ResultSet rst = connection.createStatement().executeQuery("SELECT studentId FROM Student ORDER BY studentId DESC LIMIT 1;");
            if (rst.next()) {
                String id = rst.getString("studentId");
                int newStudentId = Integer.parseInt(id.replace("S00-", "")) + 1;
                return String.format("S00-%03d", newStudentId);
            } else {
                return "S00-001";
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to generate a new id " + e.getMessage()).show();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (tblStudent.getItems().isEmpty()) {
            return "S00-001";
        } else {
            String id = getLastStudentId();
            int newStudentId = Integer.parseInt(id.replace("S", "")) + 1;
            return String.format("S00-%03d", newStudentId);
        }


    }

    private String getLastStudentId() {
        List<StudentTM> tempStudentsList = new ArrayList<>(tblStudent.getItems());
        Collections.sort(tempStudentsList);
        return tempStudentsList.get(tempStudentsList.size() - 1).getId();
    }

    private void loadDateAndTime() {
        /*set date*/
        lblDate.setText(new SimpleDateFormat("yyy-MM-dd").format(new Date()));
        /*set time*/
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalTime currentTime = LocalTime.now();
            lblTime.setText(currentTime.getHour() + ":" +
                    currentTime.getMinute() + ":" +
                    currentTime.getSecond());
        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

}
