package com.ppdai.framework.raptor.codegen.core.swagger.tool;

import com.google.common.collect.Lists;
import com.google.protobuf.DescriptorProtos;
import com.ppdai.framework.raptor.codegen.core.constant.DescriptorProtosTagNumbers;
import com.ppdai.framework.raptor.codegen.core.constant.ProtobufConstant;
import com.ppdai.framework.raptor.codegen.core.swagger.container.EnumContainer;
import com.ppdai.framework.raptor.codegen.core.swagger.container.MessageContainer;
import com.ppdai.framework.raptor.codegen.core.swagger.container.MetaContainer;
import com.ppdai.framework.raptor.codegen.core.swagger.container.ServiceContainer;
import com.ppdai.framework.raptor.codegen.core.swagger.type.FieldType;
import com.ppdai.framework.raptor.codegen.core.swagger.type.MessageType;
import com.ppdai.framework.raptor.codegen.core.swagger.type.Type;
import com.ppdai.framework.raptor.codegen.core.utils.Utils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Created by zhangyicong on 18-2-28.
 */
public class ContainerUtil {


    private static final Logger LOGGER = LoggerFactory.getLogger(Proto2SwaggerJson.class);

    /**
     * 提取enum类型
     *
     * @param fdp
     * @return
     */
    public static EnumContainer addEnums(DescriptorProtos.FileDescriptorProto fdp,
                                         MetaContainer metaContainer) {

        EnumContainer enumContainer = metaContainer.getEnumContainer();


        String className = getClassName(fdp);
        String packageName = fdp.getPackage();
        for (DescriptorProtos.EnumDescriptorProto edp : fdp.getEnumTypeList()) {
            enumContainer.addEnumProto(packageName, className, null, edp);
        }

        addEnumProto(enumContainer, packageName, className, null, fdp.getMessageTypeList());

        return enumContainer;
    }

    /**
     * 递归提取enum
     *
     * @param enumContainer
     * @param parent
     * @param dpList
     */
    private static void addEnumProto(EnumContainer enumContainer,
                                     String packageName,
                                     String className,
                                     String parent,
                                     List<DescriptorProtos.DescriptorProto> dpList) {

        for (DescriptorProtos.DescriptorProto dp : dpList) {
            String newParent = (parent != null ? parent + "." : "") + dp.getName();

            for (DescriptorProtos.EnumDescriptorProto edp : dp.getEnumTypeList()) {
                enumContainer.addEnumProto(packageName, className, newParent, edp);
            }

            addEnumProto(enumContainer, packageName, className, newParent, dp.getNestedTypeList());
        }
    }

    /**
     * 提取message类型
     *
     * @param fdp
     * @return
     */
    public static MessageContainer addMessages(DescriptorProtos.FileDescriptorProto fdp,
                                               MetaContainer metaContainer) {


        MessageContainer messageContainer = new MessageContainer();

        addMessageProto(messageContainer, fdp.getPackage(), getClassName(fdp), null, fdp.getMessageTypeList(),null);

        appendComment(messageContainer,fdp);

        metaContainer.getMessageContainer().merge(messageContainer);

        return metaContainer.getMessageContainer();
    }

    private static void appendComment(MessageContainer messageContainer, DescriptorProtos.FileDescriptorProto fdp) {
        List<DescriptorProtos.SourceCodeInfo.Location> locationList = fdp.getSourceCodeInfo().getLocationList();
        Map<List<Integer>, DescriptorProtos.SourceCodeInfo.Location> locationMap = locationList.stream()
                .collect(Collectors.toMap(DescriptorProtos.SourceCodeInfo.Location::getPathList, Function.identity(),(u1,u2)->u1));

        for (MessageType messageType : messageContainer.getMessageTypeList()) {
            for (FieldType fieldType : messageType.getFieldTypeList()) {
                DescriptorProtos.SourceCodeInfo.Location location = locationMap.get(fieldType.getPath());
                if(Objects.nonNull(location)){
                    String leadingComments = location.getLeadingComments();
                    fieldType.setLeadingComment(leadingComments);
                }
            }
        }
    }

    private static String getClassName(DescriptorProtos.FileDescriptorProto fdp) {
        String fileName = StringUtils.removeEnd(fdp.getName(), ProtobufConstant.PROTO_SUFFIX);
        if(fileName.contains(ProtobufConstant.PATH_SEPARATOR)){
            fileName = StringUtils.substringAfterLast(fileName, ProtobufConstant.PATH_SEPARATOR);
        }
        fileName = StringUtils.capitalize(fileName);
        boolean messageDup = fdp.getMessageTypeList().stream().map(DescriptorProtos.DescriptorProto::getName).anyMatch(fileName::equals);
        boolean serviceDup = fdp.getServiceList().stream().map(DescriptorProtos.ServiceDescriptorProto::getName).anyMatch(fileName::equals);
        return serviceDup || messageDup ? fileName + "OuterClass" : fileName;
    }

    /**
     * 递归提取message
     *
     * @param messageContainer
     * @param parent
     * @param dpList
     */
    private static void addMessageProto(MessageContainer messageContainer,
                                        String packageName,
                                        String className,
                                        String parent,
                                        List<DescriptorProtos.DescriptorProto> dpList,
                                        List<Integer> parentPath) {


        for (int i = 0; i < dpList.size(); i++) {
            List<Integer>  currentPath;
            if(CollectionUtils.isEmpty(parentPath)){
                currentPath =Lists.newArrayList(DescriptorProtosTagNumbers.FileDescriptorProto.MESSAGETYPE);
            }else{
                currentPath = new ArrayList<>(parentPath);
                currentPath.add(DescriptorProtosTagNumbers.DescriptorProto.NESTED_TYPE);
            }
            currentPath.add(i);

            DescriptorProtos.DescriptorProto dp = dpList.get(i);
            messageContainer.addMessageProto(packageName, className, parent, dp,currentPath);
            addMessageProto(messageContainer, packageName, className, (parent != null ? parent + "." : "") + dp.getName(),
                    dp.getNestedTypeList(),currentPath);
        }

    }

    public static void addServices(DescriptorProtos.FileDescriptorProto fdp, MetaContainer metaContainer) {
        ServiceContainer serviceContainer = metaContainer.getServiceContainer();
        getServiceContainer(fdp, serviceContainer);
    }

    /**
     * 提取service类型
     *
     * @param fdp
     * @return
     */
    public static ServiceContainer getServiceContainer(DescriptorProtos.FileDescriptorProto fdp, ServiceContainer serviceContainer) {
        String packageName = fdp.getPackage();

        List<DescriptorProtos.SourceCodeInfo.Location> locationList = fdp.getSourceCodeInfo().getLocationList();

        int serviceIndex = 0;
        for (DescriptorProtos.ServiceDescriptorProto sdp : fdp.getServiceList()) {
            List<Integer> currentPath
                    = Arrays.asList(DescriptorProtosTagNumbers.FileDescriptorProto.SERVICE, serviceIndex++);

            serviceContainer.addServiceProto(packageName, sdp, locationList, currentPath);
        }

        return serviceContainer;
    }


    public static MetaContainer getMetaContainer(File[] inputDirectories, File protocDependenciesPath) {

        MetaContainer metaContainer = new MetaContainer();
        for (File inputDirectory : inputDirectories) {
            CommandProtoc commandProtoc = CommandProtoc.configProtoPath(inputDirectory.getAbsolutePath(), protocDependenciesPath);
            ArrayList<File> allProtoFile = Lists.newArrayList();
            Utils.collectSpecificFiles(inputDirectory, ProtobufConstant.PROTO_SUFFIX, allProtoFile);
            for (File file : allProtoFile) {
                if (file.exists()) {
                    DescriptorProtos.FileDescriptorSet fileDescriptorSet = commandProtoc.invoke(file.getPath());
                    for (DescriptorProtos.FileDescriptorProto fileDescriptorProto : fileDescriptorSet.getFileList()) {
                        addEnums(fileDescriptorProto, metaContainer);
                        addMessages(fileDescriptorProto, metaContainer);
                        addServices(fileDescriptorProto, metaContainer);
                    }
                } else {
                    LOGGER.warn(file.getName() + " does not exist.");
                }
            }
        }

        enrichFiledTypeProperties(metaContainer);
        return metaContainer;

    }

    private static void enrichFiledTypeProperties(MetaContainer metaContainer) {
        MessageContainer messageContainer = metaContainer.getMessageContainer();
        for (MessageType messageType : messageContainer.getMessageTypeList()) {
            for (FieldType fieldType : messageType.getFieldTypeList()) {
                Type type = metaContainer.findTypeByFullyQualifiedPathName(fieldType.getFullyQualifiedPathName());
                if (Objects.nonNull(type)) {
                    fieldType.setClassName(type.getClassName());
                    fieldType.setPackageName(type.getPackageName());
                    fieldType.setClassName(type.getClassName());
                    fieldType.setTypeName(type.getName());
                }

            }
        }
    }
}


