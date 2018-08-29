package com.jimi.smt.eps_server.mapper;

import com.jimi.smt.eps_server.entity.Line;
import com.jimi.smt.eps_server.entity.LineExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LineMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table line
     *
     * @mbg.generated Tue Aug 14 15:05:07 CST 2018
     */
    long countByExample(LineExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table line
     *
     * @mbg.generated Tue Aug 14 15:05:07 CST 2018
     */
    int deleteByExample(LineExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table line
     *
     * @mbg.generated Tue Aug 14 15:05:07 CST 2018
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table line
     *
     * @mbg.generated Tue Aug 14 15:05:07 CST 2018
     */
    int insert(Line record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table line
     *
     * @mbg.generated Tue Aug 14 15:05:07 CST 2018
     */
    int insertSelective(Line record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table line
     *
     * @mbg.generated Tue Aug 14 15:05:07 CST 2018
     */
    List<Line> selectByExample(LineExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table line
     *
     * @mbg.generated Tue Aug 14 15:05:07 CST 2018
     */
    Line selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table line
     *
     * @mbg.generated Tue Aug 14 15:05:07 CST 2018
     */
    int updateByExampleSelective(@Param("record") Line record, @Param("example") LineExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table line
     *
     * @mbg.generated Tue Aug 14 15:05:07 CST 2018
     */
    int updateByExample(@Param("record") Line record, @Param("example") LineExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table line
     *
     * @mbg.generated Tue Aug 14 15:05:07 CST 2018
     */
    int updateByPrimaryKeySelective(Line record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table line
     *
     * @mbg.generated Tue Aug 14 15:05:07 CST 2018
     */
    int updateByPrimaryKey(Line record);
}