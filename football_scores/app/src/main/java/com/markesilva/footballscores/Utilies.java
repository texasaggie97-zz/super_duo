package com.markesilva.footballscores;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies
{
    // Being an american, I am really not sure what this is supposed to be trying to accomplish
    public static String getMatchDay(int match_day, String league_code)
    {
        final String CHAMPIONS_LEAGUE = "CL";
        if(league_code == CHAMPIONS_LEAGUE)
        {
            if (match_day <= 6)
            {
                return "Group Stages, Matchday: 6";
            }
            else if(match_day == 7 || match_day == 8)
            {
                return "First Knockout round";
            }
            else if(match_day == 9 || match_day == 10)
            {
                return "QuarterFinal";
            }
            else if(match_day == 11 || match_day == 12)
            {
                return "SemiFinal";
            }
            else
            {
                return "Final";
            }
        }
        else
        {
            return "Matchday: " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals,int away_goals)
    {
        if(home_goals < 0 || away_goals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(away_goals);
        }
    }

    public static String getScoresContentDescription(int home_goals,int away_goals)
    {
        if(home_goals < 0 || away_goals < 0)
        {
            return "no score";
        }
        else
        {
            return String.valueOf(home_goals) + " to " + String.valueOf(away_goals);
        }
    }
}
