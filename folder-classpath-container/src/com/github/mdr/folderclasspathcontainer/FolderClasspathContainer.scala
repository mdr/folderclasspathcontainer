package com.github.mdr.folderclasspathcontainer

import java.io.File
import java.io.FilenameFilter

import org.eclipse.core.resources.IMarker
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.compiler.CategorizedProblem
import org.eclipse.jdt.core.IClasspathContainer
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaModelMarker
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.internal.core.builder.JavaBuilder


object FolderClasspathContainer {

  final val ID = "com.github.mdr.folderclasspathcontainer.FolderClasspathContainer"

}

class FolderClasspathContainer(folderInfo: FolderInfo, project: IJavaProject) extends IClasspathContainer {

  private object JarFilenameFilter extends FilenameFilter {

    def accept(f: File, name: String) = name.toLowerCase.endsWith(".jar")

  }

  def getClasspathEntries: Array[IClasspathEntry] = {
    val classpathFolderFile = folderInfo.asFile(project)
    def makeLibraryEntry(jarFile: File) =
      JavaCore.newLibraryEntry(new Path(jarFile.getAbsolutePath), null, new Path("/"))
    Option(classpathFolderFile.listFiles(JarFilenameFilter)) match {
      case Some(jarFiles) =>
        jarFiles.map(makeLibraryEntry)
      case None =>
        Array(JavaCore.newLibraryEntry(new Path(folderInfo.location + "/not found").makeAbsolute, null, new Path("/")))
    }
  }
  
  // Not used for now, fi	ddly to manage lifecycle
  private def addErrorMarker(project: IJavaProject) {
    val marker = project.getProject.createMarker(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER)
    marker
      .setAttributes(
        Array(
          IMarker.MESSAGE,
          IMarker.SEVERITY,
          IJavaModelMarker.CATEGORY_ID,
          IMarker.SOURCE_ID),
        Array(
          "Cannot find folder " + folderInfo.location,
          new Integer(IMarker.SEVERITY_ERROR),
          new Integer(CategorizedProblem.CAT_BUILDPATH),
          JavaBuilder.SOURCE_ID))
  }

  def getDescription = "All Jars Within " + folderInfo.location

  def getKind = IClasspathContainer.K_APPLICATION

  def getPath: IPath = folderInfo.asEncodedPath

}