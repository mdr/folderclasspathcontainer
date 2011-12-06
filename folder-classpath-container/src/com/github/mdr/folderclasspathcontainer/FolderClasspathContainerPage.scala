package com.github.mdr.folderclasspathcontainer

import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension
import org.eclipse.jface.wizard.WizardPage
import org.eclipse.swt.events.ModifyEvent
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Combo
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.DirectoryDialog
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.SWT

class FolderClasspathContainerPage
  extends WizardPage("Folder Classpath Container", "Folder Classpath Container", null)
  with IClasspathContainerPage
  with IClasspathContainerPageExtension {

  {
    setPageComplete(false)
    setDescription("Configure folder")
  }

  private var folderCombo: Combo = _

  private var project: IJavaProject = _

  private var initFolderInfoOpt: Option[FolderInfo] = None

  def initialize(project: IJavaProject, currentEntries: Array[IClasspathEntry]) {
    this.project = project
  }

//  override def isPageComplete = Option(folderCombo).exists { fc => validate(fc.getText).isEmpty }

  def createControl(parent: Composite) {
    val composite = new Composite(parent, SWT.NULL)
    composite.setLayout(new GridLayout)
    composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))

    val folderGroup = new Composite(composite, SWT.NONE)
    val layout = new GridLayout
    layout.numColumns = 3
    folderGroup.setLayout(layout)
    folderGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))

    val folderLabel = new Label(folderGroup, SWT.NONE)
    folderLabel.setText("Folder:")

    folderCombo = new Combo(folderGroup, SWT.NONE)
    folderCombo.setText(initFolderInfoOpt.map(_.location).getOrElse(""))
    folderCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))

    folderCombo.addModifyListener(new ModifyListener {
      def modifyText(e: ModifyEvent) {
        validate(folderCombo.getText) match {
          case Some(message) =>
            setPageComplete(false)
            setErrorMessage(message)
          case None => 
            setPageComplete(true)
            setErrorMessage(null)
        }
      }
    })

    val browseButton = new Button(folderGroup, SWT.PUSH)
    browseButton.setText("Browse...")
    browseButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        chooseAFolderDialog()
      }
    })

    setControl(composite)
  }

  private def chooseAFolderDialog() {
    val dialog = new DirectoryDialog(getContainer.getShell, SWT.SAVE)
    dialog.setMessage("Choose a folder")
    dialog.setFilterPath(project.getProject.getLocation.toString)
    for (result <- Option(dialog.open)) {
      val newLocation = FolderInfo.maybeMakeRelative(result, project)
      folderCombo.setText(newLocation)
    }
  }

  def finish(): Boolean = {
    validate(folderCombo.getText).isEmpty
  }

  private def validate(location: String): Option[String] =
    if (location == "")
      Some("Must not be empty")
    else {
      val folderInfo = FolderInfo.fromLocation(location)
      val folderFile = folderInfo.asFile(project)
      if (!folderFile.exists)
        Some("Location does not exist")
      else if (!folderFile.isDirectory)
        Some("Location is not a directory")
      else
        None
    }

  def getSelection: IClasspathEntry = FolderInfo.fromLocation(folderCombo.getText).asClasspathEntry

  def setSelection(containerEntry: IClasspathEntry) {
    initFolderInfoOpt = Option(containerEntry).flatMap(entry => FolderInfo.decode(entry.getPath))
  }

}