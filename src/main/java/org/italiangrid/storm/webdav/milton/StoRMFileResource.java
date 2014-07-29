package org.italiangrid.storm.webdav.milton;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.DeletableResource;

import java.io.File;


public class StoRMFileResource extends StoRMResource implements DeletableResource, CopyableResource{

	public StoRMFileResource(StoRMResourceFactory factory, File f) {

		super(factory, f);
		
	}

	@Override
	public void delete() throws NotAuthorizedException, ConflictException,
		BadRequestException {

		getFilesystemAccess().rm(getFile());
		
	}

	@Override
	public void copyTo(CollectionResource toCollection, String name)
		throws NotAuthorizedException, BadRequestException, ConflictException {

		StoRMDirectoryResource dir = (StoRMDirectoryResource)toCollection;
		File destFile = dir.childrenFile(name);
		getFilesystemAccess().cp(getFile(), destFile);
		
	}
	
}
