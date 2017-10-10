package com.vdoc.maven.plugin.create.setup.enums;

import com.vdoc.maven.plugin.CreateSetupMojo;
import com.vdoc.maven.plugin.packaging.Packaging;
import com.vdoc.maven.plugin.packaging.impl.AppsPackaging;
import com.vdoc.maven.plugin.packaging.impl.CustomPackaging;

/**
 * Created by famaridon on 13/05/2014.
 */
public enum PackagingType {
    
    APPS(AppsPackaging.class), CUSTOM(CustomPackaging.class), PACK(AppsPackaging.class), PACK_PROCESS(AppsPackaging.class);
    
    private Class<? extends Packaging> packagingClass;
    
    PackagingType(Class<? extends Packaging> packagingClass) {
        this.packagingClass = packagingClass;
    }
    
    public Packaging getPackaging(CreateSetupMojo createSetupMojo){
        try {
            Packaging packaging = packagingClass.newInstance();
            packaging.init(createSetupMojo);
            return packaging;
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
