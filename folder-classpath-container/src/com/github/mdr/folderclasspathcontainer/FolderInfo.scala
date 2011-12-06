package com.github.mdr.folderclasspathcontainer

import java.io.File

import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

object FolderInfo {

  def decode(path: IPath): Option[FolderInfo] = {
    val id = Option(path.segment(0)).getOrElse(
      throw new IllegalArgumentException("No id segment"))
    if (id == FolderClasspathContainer.ID)
      Some({
        val filePath = path.removeFirstSegments(1)
        val firstFileSegment = Option(filePath.segment(0)).getOrElse(
          throw new IllegalArgumentException("No file segment"))
        if (firstFileSegment == ROOT_MARKER)
          new FolderInfo("/" + filePath.removeFirstSegments(1))
        else
          new FolderInfo(filePath.toString)
      })
    else
      None
  }

  def fromLocation(location: String) = new FolderInfo(location)

  final val ROOT_MARKER = "-"

  def maybeMakeRelative(location: String, project: IJavaProject): String = {
    val projectRoot = project.getProject.getLocation
    if (location startsWith projectRoot.toString) {
      val suffix = location.substring(projectRoot.toString.length)
      if (suffix startsWith "/") suffix.tail else suffix
    } else
      location
  }

}

class FolderInfo private (val location: String) {

  import FolderInfo._

  def asEncodedPath = {
    val idPath = new Path(FolderClasspathContainer.ID)
    if (isAbsolute)
      idPath.append("/" + ROOT_MARKER + "/" + location)
    else
      idPath.append("/" + location)
  }

  private def isAbsolute = location startsWith "/"

  def asPath(project: IJavaProject): IPath =
    if (isAbsolute)
      new Path(location)
    else
      project.getProject.getLocation.append(location)

  def asFile(project: IJavaProject): File = asPath(project).toFile

  def asClasspathEntry: IClasspathEntry = JavaCore.newContainerEntry(asEncodedPath)

}