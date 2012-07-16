select count(*) from UP_RAW_EVENTS where AGGREGATED=1

update UP_RAW_EVENTS set AGGREGATED=0 where AGGREGATED=1;
delete from UP_EVENT_AGGR_STATUS;
delete from UP_EVENT_SESSION_GROUPS;
delete from UP_EVENT_SESSION;
delete from UP_LOGIN_EVENT_AGGR__UIDS;
delete from UP_LOGIN_EVENT_AGGR;
delete from UP_CONCURRENT_USER_AGGR__SIDS;
delete from UP_CONCURRENT_USER_AGGR;
delete from UP_TAB_RENDER_AGGR;
delete from UP_PORTLET_EXEC_AGGR;

select * from UP_DATE_DIMENSION where DATE_ID=1219;
select * from UP_TIME_DIMENSION where TIME_ID=4321;


--Login Aggregations
SELECT DD.DD_YEAR, DD.DD_MONTH, DD.DD_DAY, TD.TD_HOUR, TD.TD_MINUTE, LEA.LOGIN_COUNT, LEA.UNIQUE_LOGIN_COUNT, LEA.DURATION, LEA.AGGR_GROUP_ID
FROM UP_LOGIN_EVENT_AGGR LEA
    LEFT JOIN UP_DATE_DIMENSION DD on LEA.DATE_DIMENSION_ID = DD.DATE_ID
    LEFT JOIN UP_TIME_DIMENSION TD on LEA.TIME_DIMENSION_ID = TD.TIME_ID
WHERE LEA.AGGR_INTERVAL='DAY'
order by DD.DD_DATE, TD.TD_TIME, LEA.AGGR_GROUP_ID;


--Concurrent user aggregations
SELECT DD.DD_YEAR, DD.DD_MONTH, DD.DD_DAY, TD.TD_HOUR, TD.TD_MINUTE, CUA.CONCURRENT_USERS, CUA.DURATION, CUA.AGGR_GROUP_ID
FROM UP_CONCURRENT_USER_AGGR CUA
    LEFT JOIN UP_DATE_DIMENSION DD on CUA.DATE_DIMENSION_ID = DD.DATE_ID
    LEFT JOIN UP_TIME_DIMENSION TD on CUA.TIME_DIMENSION_ID = TD.TIME_ID
WHERE CUA.AGGR_INTERVAL='FIVE_MINUTE' AND CUA.AGGR_GROUP_ID=5
order by DD.DD_DATE, TD.TD_TIME, CUA.AGGR_GROUP_ID;


--Tab Render aggregations
SELECT DD.DD_YEAR, DD.DD_MONTH, DD.DD_DAY, TD.TD_HOUR, TD.TD_MINUTE, TRA.AGGR_TAB_ID, TRA.TIME_COUNT, TRA.GEOMETRIC_MEAN_TIME,
        TRA.MAX_TIME, TRA.MEAN_TIME, TRA.MIN_TIME, TRA.POPULATION_VARIANCE_TIME, TRA.SECOND_MOMENT_TIME, TRA.STD_DEVIATION_TIME,
        TRA.SUM_TIME, TRA.SUM_OF_LOGS_TIME, TRA.VARIANCE_TIME
FROM UP_TAB_RENDER_AGGR TRA
    LEFT JOIN UP_DATE_DIMENSION DD on TRA.DATE_DIMENSION_ID = DD.DATE_ID
    LEFT JOIN UP_TIME_DIMENSION TD on TRA.TIME_DIMENSION_ID = TD.TIME_ID
WHERE TRA.AGGR_INTERVAL='HOUR' AND TRA.AGGR_GROUP_ID=5
order by DD.DD_DATE, TD.TD_TIME, TRA.AGGR_GROUP_ID;


--Portlet Exec aggregations
SELECT DD.DD_YEAR, DD.DD_MONTH, DD.DD_DAY, TD.TD_HOUR, TD.TD_MINUTE, PEA.AGGR_PORTLET_ID, PEA.EXECUTION_TYPE, PEA.TIME_COUNT, PEA.GEOMETRIC_MEAN_TIME,
        PEA.MAX_TIME, PEA.MEAN_TIME, PEA.MIN_TIME, PEA.POPULATION_VARIANCE_TIME, PEA.SECOND_MOMENT_TIME, PEA.STD_DEVIATION_TIME,
        PEA.SUM_TIME, PEA.SUM_OF_LOGS_TIME, PEA.VARIANCE_TIME
FROM UP_PORTLET_EXEC_AGGR PEA
    LEFT JOIN UP_DATE_DIMENSION DD on PEA.DATE_DIMENSION_ID = DD.DATE_ID
    LEFT JOIN UP_TIME_DIMENSION TD on PEA.TIME_DIMENSION_ID = TD.TIME_ID
WHERE PEA.AGGR_INTERVAL='HOUR' AND PEA.AGGR_GROUP_ID=5
order by DD.DD_DATE, TD.TD_TIME, PEA.AGGR_GROUP_ID;

select TAB_NAME, count(*)
from UP_TAB_RENDER_AGGR
group by TAB_NAME



select distinct USER_NAME
from UP_RAW_EVENTS;




SELECT portletexe0_.id                       AS ID78_, 
       portletexe0_.aggr_group_id            AS AGGR33_78_, 
       portletexe0_.date_dimension_id        AS DATE34_78_, 
       portletexe0_.duration                 AS DURATION78_, 
       portletexe0_.aggr_interval            AS AGGR3_78_, 
       portletexe0_.time_dimension_id        AS TIME35_78_, 
       portletexe0_.stats_complete           AS STATS4_78_, 
       portletexe0_.time_count               AS TIME5_78_, 
       portletexe0_.geometric_mean_time      AS GEOMETRIC6_78_, 
       portletexe0_.max_time                 AS MAX7_78_, 
       portletexe0_.mean_time                AS MEAN8_78_, 
       portletexe0_.min_time                 AS MIN9_78_, 
       portletexe0_.population_variance_time AS POPULATION10_78_, 
       portletexe0_.second_moment_time       AS SECOND11_78_, 
       portletexe0_.std_deviation_time       AS STD12_78_, 
       portletexe0_.cm_max_n                 AS CM13_78_, 
       portletexe0_.cm_max_value             AS CM14_78_, 
       portletexe0_.cm_min_n                 AS CM15_78_, 
       portletexe0_.cm_min_value             AS CM16_78_, 
       portletexe0_.cm_2mmnt_m2              AS CM17_78_, 
       portletexe0_.cm_1mmnt_dev             AS CM18_78_, 
       portletexe0_.cm_1mmnt_m1              AS CM19_78_, 
       portletexe0_.cm_1mmnt_n               AS CM20_78_, 
       portletexe0_.cm_1mmnt_ndev            AS CM21_78_, 
       portletexe0_.cm_sum_n                 AS CM22_78_, 
       portletexe0_.cm_sum_value             AS CM23_78_, 
       portletexe0_.cm_sumologs_n            AS CM24_78_, 
       portletexe0_.cm_sumologs_value        AS CM25_78_, 
       portletexe0_.cm_sumosqrs_n            AS CM26_78_, 
       portletexe0_.cm_sumosqrs_value        AS CM27_78_, 
       portletexe0_.sum_time                 AS SUM28_78_, 
       portletexe0_.sum_of_logs_time         AS SUM29_78_, 
       portletexe0_.sumsq_time               AS SUMSQ30_78_, 
       portletexe0_.variance_time            AS VARIANCE31_78_, 
       portletexe0_.aggr_portlet_id          AS AGGR36_78_, 
       portletexe0_.execution_type           AS EXECUTION32_78_ 
FROM   up_portlet_exec_aggr portletexe0_ 
WHERE  portletexe0_.date_dimension_id =? 
       AND portletexe0_.time_dimension_id =? 
       AND portletexe0_.aggr_interval =? 
       AND portletexe0_.aggr_portlet_id =? 
       AND portletexe0_.execution_type =? 