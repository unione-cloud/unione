package com.unione.cloud;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.beetl.core.ReThrowConsoleErrorHandler;
import org.beetl.sql.core.ConnectionSource;
import org.beetl.sql.core.ConnectionSourceHelper;
import org.beetl.sql.core.Interceptor;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SQLManagerBuilder;
import org.beetl.sql.core.UnderlinedNameConversion;
import org.beetl.sql.core.db.MySqlStyle;
import org.beetl.sql.core.meta.MetadataManager;
import org.beetl.sql.ext.DebugInterceptor;
import org.beetl.sql.gen.SourceBuilder;
import org.beetl.sql.gen.SourceFilter;
import org.beetl.sql.gen.simple.EntitySourceBuilder;

import com.unione.cloud.beetsql.ext.FunIsBaseField;
import com.unione.cloud.codegen.ApiSourceBuilder;
import com.unione.cloud.codegen.BuilderFactory;
import com.unione.cloud.codegen.PojoSourceBuilder;
import com.unione.cloud.codegen.SimpleUnioneProject;
import com.unione.cloud.codegen.SqlMdSourceBuilder;
import com.zaxxer.hikari.HikariDataSource;

import cn.hutool.core.date.DateUtil;

public class CodeBuilderFactoryTest {
	
	private static   DataSource datasource() {
		HikariDataSource ds = new HikariDataSource();
    //内存数据库
		ds.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/unione?serverTimezone=Asia/Shanghai&autoReconnect=true&useUnicode=true&characterEncoding=utf8");
		ds.setUsername("unione");
		ds.setPassword("unione@db");
		ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
		return ds;
	}

	private  static SQLManager getSQLManager(){
    //得到一个数据源
		DataSource dataSource = datasource();
    //得到一个ConnectionSource， 单数据源
		ConnectionSource source = ConnectionSourceHelper.getSingle(dataSource);
    //SQLManagerBuilder 唯一必须的参数就是ConnectionSource
		SQLManagerBuilder builder = new SQLManagerBuilder(source);
    //命名转化，数据库表和列名下划线风格，转化成Java对应的首字母大写，比如create_time 对应ceateTime
		builder.setNc(new UnderlinedNameConversion());
    //拦截器，非必须，这里设置一个debug拦截器，可以详细查看执行后的sql和sql参数
		builder.setInters(new Interceptor[]{new DebugInterceptor()});
    //数据库风格，因为用的是H2,所以使用H2Style,
		builder.setDbStyle(new MySqlStyle());
		SQLManager sqlManager = builder.build();
		return sqlManager;
	}

	
	public static void main(String[] args) {
		
		SQLManager sqlManager=getSQLManager();

		List<SourceBuilder> sourceBuilder = new ArrayList<>();
		SourceBuilder entityBuilder = new PojoSourceBuilder("system");
		SourceBuilder apiBuilder = new ApiSourceBuilder("system");
//		SourceBuilder mdBuilder = new SqlMdSourceBuilder("system");

		sourceBuilder.add(entityBuilder);
//		sourceBuilder.add(mdBuilder);
		sourceBuilder.add(apiBuilder);

		BuilderFactory factory = new BuilderFactory(sqlManager,sourceBuilder);
		factory.setEntityParentClass("Pojo");
		
		//如果有错误，抛出异常而不是继续运行1
		EntitySourceBuilder.getGroupTemplate().setErrorHandler(new ReThrowConsoleErrorHandler());
		EntitySourceBuilder.getGroupTemplate().registerFunction("isBaseField",new FunIsBaseField());
		
		SimpleUnioneProject mavenProject = new SimpleUnioneProject("com.unione.cloud");
		mavenProject.setRoot("d://codegen_"+DateUtil.format(new Date(), "yyyyMMdd-HHmmss"));
		
		//可以在控制台看到生成的所有代码
//		factory.genAll(mavenProject, new SourceFilter() {
//			@Override
//			public boolean accept(MetadataManager metadataManager, String tableName) {
//				return tableName.startsWith("sys_data");
//			}
//		});
		
		factory.gen("sys_change_log",mavenProject);
		factory.gen("sys_trend_rss",mavenProject);

//		factory.genAll(mavenProject);
	}

}
