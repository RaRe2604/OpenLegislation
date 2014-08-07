package gov.nysenate.openleg.dao.bill;

import gov.nysenate.openleg.dao.base.*;

public enum SqlVetoQuery implements BasicSqlQuery{
    SELECT_VETO_MESSAGE_SQL(
        "SELECT * FROM ${schema}." + SqlTable.BILL_VETO + "\n" +
        "WHERE veto_number = :vetoNumber AND year = :year"
    ),
    UPDATE_VETO_MESSAGE_SQL(
        "UPDATE ${schema}." + SqlTable.BILL_VETO + "\n" +
        "SET bill_print_no = :printNum, session_year = :sessionYear, type = CAST(:type AS ${schema}.veto_type), " + "\n" +
            "chapter = :chapter, page = :page, line_start = :lineStart, line_end = :lineEnd, " + "\n" +
            "date = :date, signer = :signer, memo_text = :memoText, " + "\n" +
            "modified_date_time = :modifiedDateTime, last_fragment_id = :lastFragmentId" + "\n" +
        "WHERE veto_number = :vetoNumber AND year = :year"
    ),
    INSERT_VETO_MESSAGE_SQL(
        "INSERT INTO ${schema}." + SqlTable.BILL_VETO + "\n" +
            "( veto_number, year, bill_print_no, session_year, chapter, page, line_start, line_end, signer, date," + "\n" +
            " memo_text, type, modified_date_time, published_date_time, last_fragment_id )" +"\n" +
        "VALUES (:vetoNumber, :year, :printNum, :sessionYear, :chapter, :page, :lineStart, :lineEnd, :signer, :date, " + "\n" +
            " :memoText, CAST(:type AS ${schema}.veto_type), :modifiedDateTime, :publishedDateTime, :lastFragmentId)"
    ),
    SELECT_BILL_VETOES_SQL(
        "SELECT * FROM ${schema}." + SqlTable.BILL_VETO + "\n" +
        "WHERE bill_print_no = :printNum AND session_year = :sessionYear" + "\n" +
        "ORDER BY year, veto_number"
    )
    ;

    private String sql;

    SqlVetoQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql(String envSchema) {
        return SqlQueryUtils.getSqlWithSchema(sql, envSchema);
    }

    @Override
    public String getSql(String envSchema, LimitOffset limitOffset) {
        return SqlQueryUtils.getSqlWithSchema(sql, envSchema, limitOffset);
    }

    @Override
    public String getSql(String envSchema, OrderBy orderBy, LimitOffset limitOffset) {
        return SqlQueryUtils.getSqlWithSchema(this.sql, envSchema, orderBy, limitOffset);
    }
}
