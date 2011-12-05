package com.github.mdr.folderclasspathcontainer

import java.io.File
import java.io.FilenameFilter
import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core._
import org.eclipse.core.runtime.Path

object FolderClasspathContainer {

  final val ID = "com.github.mdr.folderclasspathcontainer.FolderClasspathContainer"

}

class FolderClasspathContainer(path: IPath, project: IJavaProject) extends IClasspathContainer {

  private val folderInfo = FolderInfo(path)

  private object JarFilenameFilter extends FilenameFilter {

    def accept(f: File, name: String) = name.toLowerCase.endsWith(".jar")

  }

  def getClasspathEntries: Array[IClasspathEntry] = {
    val classpathFolderFile = folderInfo.asFile(project)
    def makeLibraryEntry(jarFile: File) =
      JavaCore.newLibraryEntry(new Path(jarFile.getAbsolutePath), null, new Path("/"))
    val jarFiles = Option(classpathFolderFile.listFiles(JarFilenameFilter)).getOrElse(Array[File]())
    jarFiles.map(makeLibraryEntry)
  }

  def getDescription = "All Jars Within " + folderInfo.location

  def getKind = IClasspathContainer.K_APPLICATION

  def getPath: IPath = path

}