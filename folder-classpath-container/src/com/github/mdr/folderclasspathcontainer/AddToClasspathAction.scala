package com.github.mdr.folderclasspathcontainer

import scala.PartialFunction.cond
import scala.PartialFunction.condOpt

import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.IAdaptable
import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jface.action.IAction
import org.eclipse.jface.viewers.ISelection
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.ui.IObjectActionDelegate
import org.eclipse.ui.IWorkbenchPart

class AddToClasspathAction extends IObjectActionDelegate {

  object SelectedItems {
    def unapplySeq(selection: ISelection): Option[List[Any]] = condOpt(selection) {
      case structuredSelection: IStructuredSelection => structuredSelection.toArray.toList
    }
  }

  implicit def adaptableToPimpedAdaptable(adaptable: IAdaptable): PimpedAdaptable = new PimpedAdaptable(adaptable)

  class PimpedAdaptable(adaptable: IAdaptable) {

    def adaptTo[T](implicit m: Manifest[T]): T = adaptable.getAdapter(m.erasure).asInstanceOf[T]

    def adaptToSafe[T](implicit m: Manifest[T]): Option[T] = Option(adaptable.getAdapter(m.erasure).asInstanceOf[T])

  }

  private var selectionOption: Option[ISelection] = None

  private def containsClasspathEntry(project: IJavaProject, folder: IFolder): Boolean = {
    project.getRawClasspath.exists { classpathEntry =>
      cond(FolderInfo.decode(classpathEntry.getPath)) {
        case Some(folderInfo) => samePath(folderInfo.asPath(project), folder.getLocation)
      }
    }
  }

  private def samePath(path1: IPath, path2: IPath) =
    path1.toFile.getCanonicalPath == path2.toFile.getCanonicalPath

  override def selectionChanged(action: IAction, selection: ISelection) {
    this.selectionOption = Option(selection)
    val enabled = (for {
      selection <- selectionOption
      folder <- selectedFolder(selection)
      project = JavaCore.create(folder.getProject)
    } yield !containsClasspathEntry(project, folder)).getOrElse(false)
    action.setEnabled(enabled)
  }

  override def run(action: IAction) =
    for {
      selection <- selectionOption
      folder <- selectedFolder(selection)
    } {
      val project = JavaCore.create(folder.getProject)
      val folderLocation = FolderInfo.maybeMakeRelative(folder.getLocation.toString, project)
      val containerEntry = FolderInfo.fromLocation(folderLocation).asClasspathEntry
      val newClasspathEntries = project.getRawClasspath :+ containerEntry
      project.setRawClasspath(newClasspathEntries, null);
    }

  def setActivePart(action: IAction, targetPart: IWorkbenchPart) { }

  private def selectedFolder(selection: ISelection): Option[IFolder] = {
    val adaptableOpt = condOpt(selection) {
      case SelectedItems(adaptable: IAdaptable) => adaptable
    }
    for {
      adaptable <- adaptableOpt
      resource <- adaptable.adaptToSafe[IResource]
      folder <- condOpt(resource) { case folder: IFolder => folder }
    } yield folder
  }

}