package com.atex.custom.camel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.camel.component.file.GenericFile;
import org.apache.commons.io.IOUtils;

import com.atex.custom.parser.ITextParser;
import com.atex.onecms.app.dam.DamContentBean;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.ContentWriteBuilder;
import com.atex.onecms.content.InsertionInfoAspectBean;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.metadata.MetadataInfo;
import com.polopoly.metadata.Metadata;

public class FiegFeedProcessor extends BaseFeedProcessor {

    private static final Subject SYSTEM_SUBJECT = new Subject("98", null);

    @Override
    protected void processFile(final GenericFile file) throws Exception {

        BufferedInputStream bis = null;
        try {
            checkFileStability(file);

            String filePath = file.getAbsoluteFilePath();

            bis = new BufferedInputStream(new FileInputStream(filePath));


            Class<?> c = Class.forName(getParserClass());
            ITextParser parser = (ITextParser) c.newInstance();

            parser.setFieldValueMapping(fieldValueMapping);

            // contentData
            final DamContentBean damContentBean = parser.parseFile(new File(filePath));

            // add p.InsertionInfo
            final InsertionInfoAspectBean insertionInfoAspectBean = new InsertionInfoAspectBean(securityParentContentId);


            // add atex.metadata
            final MetadataInfo metadataInfo = new MetadataInfo();
            Set<String> set = new HashSet<>();
            set.add("p.StandardCategorization");
            metadataInfo.setTaxonomyIds(set);
            Metadata metadata = new Metadata();

            // metadata empty ... to be defined if any available metadata for these feeds
            metadataInfo.setMetadata(metadata);


            final ContentWriteBuilder<DamContentBean> cwb = createContentWriteBuilder(damContentBean);

            cwb.aspect("p.InsertionInfo", insertionInfoAspectBean);
            cwb.aspect("atex.Metadata", metadataInfo);

            final ContentWrite<DamContentBean> content = cwb.buildCreate();
            getContentManager().create(content, SYSTEM_SUBJECT);

        } finally {
            IOUtils.closeQuietly(bis);
        }
    }

}
