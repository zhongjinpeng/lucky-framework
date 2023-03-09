package io.lucky.database.utils;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.ITypeConvert;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import com.baomidou.mybatisplus.generator.fill.Property;
import com.baomidou.mybatisplus.generator.keywords.MySqlKeyWordsHandler;
import com.baomidou.mybatisplus.generator.query.SQLQuery;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

@Component
public class CodeGenerateUtils {

    private @Value("${spring.datasource.url:}")
    String url;
    private @Value("${spring.datasource.username:}")
    String username;
    private @Value("${spring.datasource.password:}")
    String password;

    public void generate(String moduleName, String parent, String... tableName) {

        Assert.notNull(moduleName, "moduleName is null!");
        Assert.notNull(parent, "parent is null!");
        Assert.notNull(tableName, "tableName is null!");

        String projectPath = System.getProperty("user.dir");
        FastAutoGenerator.create(new DataSourceConfig.Builder(url, username, password)
                        .dbQuery(new MySqlQuery()).databaseQueryClass(SQLQuery.class)
                        .typeConvert(new MySqlTypeConvertCustom()))
                .globalConfig(builder -> {
                    builder.author("baomidou")
                            .enableSwagger()
                            .outputDir(projectPath + "/src/main/java")
                            .dateType(DateType.TIME_PACK)
                            .commentDate("yyyy-MM-dd HH:mm:ss");
                })
                .packageConfig(builder -> {
                    builder.parent(parent)
                            .moduleName(moduleName)
                            .entity("dao.entity")
                            .service("service")
                            .serviceImpl("service.impl")
                            .mapper("dao.mapper")
                            .xml("mapper.xml")
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, projectPath + "/src/main/resources/mapper"))
                            .build();
                })
                .strategyConfig(builder -> {
                    builder.addInclude(StringUtils.join(Arrays.asList(tableName), ","))
                            .addTablePrefix()
                            .entityBuilder()
                            .enableFileOverride()
                            .enableChainModel()
                            .enableLombok()
                            .enableRemoveIsPrefix()
                            .enableTableFieldAnnotation()
                            .enableActiveRecord()
                            .idType(IdType.AUTO)
                            .logicDeleteColumnName("is_deleted")
                            .logicDeletePropertyName("isDeleted")
                            .addTableFills(new Column("create_time", FieldFill.INSERT))
                            .addTableFills(new Property("updateTime", FieldFill.UPDATE))
                            .mapperBuilder().enableFileOverride().enableBaseColumnList().enableBaseResultMap()
                            .controllerBuilder().enableFileOverride().enableRestStyle()
                            .serviceBuilder().enableFileOverride();
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    static class MySqlTypeConvertCustom extends MySqlTypeConvert implements ITypeConvert {
        @Override
        public IColumnType processTypeConvert(@NotNull GlobalConfig globalConfig, String fieldType) {
            String t = fieldType.toLowerCase();
            if (t.contains("tinyint")) {
                return DbColumnType.INTEGER;
            }
            return super.processTypeConvert(globalConfig, fieldType);
        }
    }
}
