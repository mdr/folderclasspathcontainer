package com.github.mdr.folderclasspathcontainer

import org.eclipse.jdt.ui.wizards.IClasspathContainerPage
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension
import org.eclipse.jface.wizard.WizardPage
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.events._
import org.eclipse.swt.SWT
import org.eclipse.jdt.core.JavaCore
import org.eclipse.core.runtime.Path
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Combo
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.DirectoryDialog

class FolderClasspathContainerPage
  extends WizardPage("Folder Classpath Container", "Folder Classpath Container", null)
  with IClasspathContainerPage
  with IClasspathContainerPageExtension {

  {
    setPageComplete(true)
    setDescription("Configure folder")
  }

  private var folderCombo: Combo = _

  private var project: IJavaProject = _

  private var initFolderInfoOpt: Option[FolderInfo] = None

  def initialize(project: IJavaProject, currentEntries: Array[IClasspathEntry]) {
    this.project = project
  }

  def createControl(parent: Composite) {
    val composite = new Composite(parent, SWT.NULL)
    composite.setLayout(new GridLayout)
    composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL))

    val folderGroup = new Composite(composite, SWT.NONE)
    val layout = new GridLayout
    layout.numColumns = 3
    folderGroup.setLayout(layout)
    folderGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL))

    val folderLabel = new Label(folderGroup, SWT.NONE)
    folderLabel.setText("Folder:")

    folderCombo = new Combo(folderGroup, SWT.NONE)
    folderCombo.setText(initFolderInfoOpt.map(_.location).getOrElse(""))
    folderCombo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL))

    folderCombo.addModifyListener(new ModifyListener {
      def modifyText(e: ModifyEvent) {
        validate(folderCombo.getText) match {
          case Some(message) => setErrorMessage(message)
          case None => setErrorMessage(null)
        }
      }
    })

    val browseButton = new Button(folderGroup, SWT.PUSH)
    browseButton.setText("Browse...")
    browseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL))
    browseButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        handleBrowse()
      }
    })

    setControl(composite)
  }

  private def handleBrowse() {
    val dialog = new DirectoryDialog(getContainer.getShell, SWT.SAVE)
    dialog.setMessage("Pick a folder")
    dialog.setFilterPath(projectRoot.toString)
    for (result <- Option(dialog.open)) {
      val newLocation =
        if (result startsWith projectRoot.toString) {
          val suffix = result.substring(projectRoot.toString.length)
          if (suffix startsWith "/")
            suffix.tail
          else suffix
        } else
          result
      folderCombo.setText(newLocation)
    }
  }

  private def projectRoot = project.getProject.getLocation

  def finish(): Boolean = {
    validate(folderCombo.getText).isEmpty
  }

  private def validate(location: String): Option[String] = {
    val folderInfo = new FolderInfo(location)
    val folderFile = folderInfo.asFile(project)
    if (!folderFile.exists)
      Some("Location does not exist")
    else if (!folderFile.isDirectory)
      Some("Location is not a directory")
    else
      None
  }

  def getSelection: IClasspathEntry = JavaCore.newContainerEntry(new FolderInfo(folderCombo.getText).asPath)

  def setSelection(containerEntry: IClasspathEntry) {
    initFolderInfoOpt = Option(containerEntry).map(entry => FolderInfo(entry.getPath))
  }

}