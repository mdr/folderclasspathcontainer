package com.github.mdr.folderclasspathcontainer

import org.eclipse.core.runtime._
import org.eclipse.jdt.core.IJavaProject
import java.io.File

object FolderInfo {

  def apply(path: IPath): FolderInfo = {
    val id = Option(path.segment(0)).getOrElse(
      throw new IllegalArgumentException("No id segment"))
    if (id != FolderClasspathContainer.ID)
      throw new IllegalArgumentException("Incorrect classpath container id: " + id)
    var filePath = path.removeFirstSegments(1)
    val firstFileSegment = Option(filePath.segment(0)).getOrElse(
      throw new IllegalArgumentException("No file segment"))
    if (firstFileSegment == ROOT_MARKER)
      new FolderInfo("/" + filePath.removeFirstSegments(1))
    else
      new FolderInfo(filePath.toString)
  }

  final val ROOT_MARKER = "-"
}

class FolderInfo(val location: String) {

  import FolderInfo._

  def asPath = {
    val idPath = new Path(FolderClasspathContainer.ID)
    if (isAbsolute)
      idPath.append("/" + ROOT_MARKER + "/" + location)
    else
      idPath.append("/" + location)
  }

  private def isAbsolute = location startsWith "/"

  def asFile(project: IJavaProject): File =
    if (isAbsolute)
      new File(location)
    else {
      val projectRoot = project.getProject.getLocation.makeAbsolute.toFile
      new File(projectRoot, location)
    }

}