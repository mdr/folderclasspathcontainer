package com.github.mdr.folderclasspathcontainer

import java.io.File
import java.io.FilenameFilter
import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core._
import org.eclipse.core.runtime.Path

class FolderClasspathContainer(path: IPath, project: IJavaProject) extends IClasspathContainer {

  
  private val folderName = path.removeFirstSegments(1).toString
  
  private val classpathFolder: File = {
    val projectRoot = project.getProject.getLocation.makeAbsolute.toFile
    new File(projectRoot, folderName)
  }

  private object JarFilenameFilter extends FilenameFilter {

    def accept(f: File, name: String) = name.toLowerCase.endsWith(".jar")

  }

  def getClasspathEntries: Array[IClasspathEntry] = {
    def makeLibraryEntry(jarFile: File) =
      JavaCore.newLibraryEntry(new Path(jarFile.getAbsolutePath), null, new Path("/"))
    val jarFiles = Option(classpathFolder.listFiles(JarFilenameFilter)).getOrElse(Array())
    jarFiles.map(makeLibraryEntry)
  }

  def getDescription = "Jars within " + folderName

  def getKind = IClasspathContainer.K_APPLICATION

  def getPath: IPath = path

}