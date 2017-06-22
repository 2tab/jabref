package org.jabref.gui.sharelatex;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

import org.jabref.gui.AbstractController;
import org.jabref.gui.StateManager;
import org.jabref.logic.sharelatex.ShareLatexManager;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.preferences.PreferencesService;

public class ShareLatexProjectDialogController extends AbstractController<ShareLatexProjectDialogViewModel> {

    @FXML private TableColumn<ShareLatexProjectViewModel, Boolean> colActive;
    @FXML private TableColumn<ShareLatexProjectViewModel, String> colTitle;
    @FXML private TableColumn<ShareLatexProjectViewModel, String> colOwner;
    @FXML private TableColumn<ShareLatexProjectViewModel, String> colLastModified;
    @FXML private TableView<ShareLatexProjectViewModel> tblProjects;
    @Inject private ShareLatexManager manager;
    @Inject private StateManager stateManager;
    @Inject private PreferencesService preferences;

    @FXML
    private void initialize() {
        viewModel = new ShareLatexProjectDialogViewModel(stateManager, manager);
        try {
            viewModel.addProjects(manager.getProjects());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        tblProjects.setEditable(true);
        colActive.setEditable(true);

        colActive.setCellFactory(CheckBoxTableCell.forTableColumn(colActive));

        colActive.setCellValueFactory(cellData -> cellData.getValue().isActiveProperty());
        colTitle.setCellValueFactory(cellData -> cellData.getValue().getProjectTitle());
        colOwner.setCellValueFactory(cellData -> cellData.getValue().getOwner());
        colLastModified.setCellValueFactory(cellData -> cellData.getValue().getLastUpdated());
        setBindings();

    }

    private void setBindings() {
        tblProjects.itemsProperty().bindBidirectional(viewModel.projectsProperty());
    }

    @FXML
    private void synchronizeLibrary() {

        Optional<ShareLatexProjectViewModel> projects = viewModel.projectsProperty().filtered(x -> x.isActive())
                .stream().findFirst();

        if (projects.isPresent() && stateManager.getActiveDatabase().isPresent()) {
            String projectID = projects.get().getProjectId();
            BibDatabaseContext database = stateManager.getActiveDatabase().get();

            manager.startWebSocketHandler(projectID, database, preferences.getImportFormatPreferences());
        }

        cancelAndClose();

    }

    @FXML
    private void cancelAndClose() {
        getStage().close();
    }
}
