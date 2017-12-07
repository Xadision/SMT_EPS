package com.jimi.smt.eps_server.entity;

import java.util.Date;

public class Operation {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.id
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private Long id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.operator
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private String operator;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.time
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private Date time;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.type
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private Integer type;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.result
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private String result;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.lineseat
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private String lineseat;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.old_material_no
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private String oldMaterialNo;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.scanlineseat
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private String scanlineseat;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.material_no
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private String materialNo;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.remark
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private String remark;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.program_id
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private String programId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.line
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private String line;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.work_order
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private String workOrder;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column operation.board_type
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    private Integer boardType;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.id
     *
     * @return the value of operation.id
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.id
     *
     * @param id the value for operation.id
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.operator
     *
     * @return the value of operation.operator
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public String getOperator() {
        return operator;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.operator
     *
     * @param operator the value for operation.operator
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setOperator(String operator) {
        this.operator = operator == null ? null : operator.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.time
     *
     * @return the value of operation.time
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public Date getTime() {
        return time;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.time
     *
     * @param time the value for operation.time
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.type
     *
     * @return the value of operation.type
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public Integer getType() {
        return type;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.type
     *
     * @param type the value for operation.type
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.result
     *
     * @return the value of operation.result
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public String getResult() {
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.result
     *
     * @param result the value for operation.result
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setResult(String result) {
        this.result = result == null ? null : result.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.lineseat
     *
     * @return the value of operation.lineseat
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public String getLineseat() {
        return lineseat;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.lineseat
     *
     * @param lineseat the value for operation.lineseat
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setLineseat(String lineseat) {
        this.lineseat = lineseat == null ? null : lineseat.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.old_material_no
     *
     * @return the value of operation.old_material_no
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public String getOldMaterialNo() {
        return oldMaterialNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.old_material_no
     *
     * @param oldMaterialNo the value for operation.old_material_no
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setOldMaterialNo(String oldMaterialNo) {
        this.oldMaterialNo = oldMaterialNo == null ? null : oldMaterialNo.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.scanlineseat
     *
     * @return the value of operation.scanlineseat
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public String getScanlineseat() {
        return scanlineseat;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.scanlineseat
     *
     * @param scanlineseat the value for operation.scanlineseat
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setScanlineseat(String scanlineseat) {
        this.scanlineseat = scanlineseat == null ? null : scanlineseat.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.material_no
     *
     * @return the value of operation.material_no
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public String getMaterialNo() {
        return materialNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.material_no
     *
     * @param materialNo the value for operation.material_no
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setMaterialNo(String materialNo) {
        this.materialNo = materialNo == null ? null : materialNo.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.remark
     *
     * @return the value of operation.remark
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public String getRemark() {
        return remark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.remark
     *
     * @param remark the value for operation.remark
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.program_id
     *
     * @return the value of operation.program_id
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public String getProgramId() {
        return programId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.program_id
     *
     * @param programId the value for operation.program_id
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setProgramId(String programId) {
        this.programId = programId == null ? null : programId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.line
     *
     * @return the value of operation.line
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public String getLine() {
        return line;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.line
     *
     * @param line the value for operation.line
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setLine(String line) {
        this.line = line == null ? null : line.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.work_order
     *
     * @return the value of operation.work_order
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public String getWorkOrder() {
        return workOrder;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.work_order
     *
     * @param workOrder the value for operation.work_order
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setWorkOrder(String workOrder) {
        this.workOrder = workOrder == null ? null : workOrder.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column operation.board_type
     *
     * @return the value of operation.board_type
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public Integer getBoardType() {
        return boardType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column operation.board_type
     *
     * @param boardType the value for operation.board_type
     *
     * @mbggenerated Tue Nov 21 17:14:15 CST 2017
     */
    public void setBoardType(Integer boardType) {
        this.boardType = boardType;
    }
}