package com.jimi.smt.eps_server.mapper;

import com.jimi.smt.eps_server.entity.Operation;
import com.jimi.smt.eps_server.entity.OperationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OperationMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table operation
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    int countByExample(OperationExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table operation
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    int deleteByExample(OperationExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table operation
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table operation
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    int insert(Operation record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table operation
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    int insertSelective(Operation record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table operation
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    List<Operation> selectByExample(OperationExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table operation
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    Operation selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table operation
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    int updateByExampleSelective(@Param("record") Operation record, @Param("example") OperationExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table operation
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    int updateByExample(@Param("record") Operation record, @Param("example") OperationExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table operation
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    int updateByPrimaryKeySelective(Operation record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table operation
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    int updateByPrimaryKey(Operation record);
}