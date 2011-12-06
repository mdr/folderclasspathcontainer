package com.github.mdr.folderclasspathcontainer

import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core.ClasspathContainerInitializer
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import java.util.logging.Logger

class FolderClasspathContainerInitializer extends ClasspathContainerInitializer {

  def initialize(containerPath: IPath, project: IJavaProject) {
    for (folderInfo <- FolderInfo.decode(containerPath)) {
      val container = new FolderClasspathContainer(folderInfo, project)
      JavaCore.setClasspathContainer(containerPath, Array(project), Array(container), null)
    }
  }

}