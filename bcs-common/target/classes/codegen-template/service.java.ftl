package ${package.Service};

import ${package.Entity}.${entity};
import ${package.Entity}.vo.${entity}Vo;
import ${package.Mapper}.${table.mapperName};
import ${superServiceClassPackage};
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * ${table.comment!} 服务类
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ${table.serviceName} extends ${superServiceClass}<${table.mapperName}, ${entity}, ${entity}Vo>  {

}
