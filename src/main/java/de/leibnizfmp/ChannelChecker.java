package de.leibnizfmp;

import ij.IJ;

public class ChannelChecker {

    // checkChannelSettings checks if the chosen channel settings are valid and each channel is unique
    static boolean checkChannelSetting( String nucChannel, String cytoChannel, String orgaChannel ) {

        boolean nucChannelChecker;

        assert nucChannel != null;
        if (nucChannel.equals( "ignore" ) || nucChannel.equals( "select" ) ) {

            IJ.log("Invalid nucleus channel setting!");
            nucChannelChecker = false;

        } else {

            nucChannelChecker = true;
        }

        boolean cytoChannelChecker;

        assert cytoChannel != null;
        if (cytoChannel.equals( "ignore" ) || cytoChannel.equals( "select" ) ) {

            IJ.log("Invalid cytoplasm channel setting!");
            cytoChannelChecker = false;


        } else {

            cytoChannelChecker = true;

        }

        boolean orgaChannelChecker;

        assert orgaChannel!= null;
        if (orgaChannel.equals( "ignore" ) || orgaChannel.equals( "select" ) ) {

            IJ.log("Invalid organelle channel setting!");
            orgaChannelChecker = false;

        } else {

            orgaChannelChecker = true;
        }

        boolean uniqueChannelSetting;

        if ( nucChannel.equals( cytoChannel ) || cytoChannel.equals( orgaChannel ) || nucChannel.equals( orgaChannel ) ) {

            IJ.log("A channel number is not unique!");
            uniqueChannelSetting = false;

        } else {

            uniqueChannelSetting = true;

        }

        return nucChannelChecker & cytoChannelChecker & orgaChannelChecker & uniqueChannelSetting;

    }

    // The channelNumber method returns 0 if ignored or select is chosen otherwise it returns an integer
    static int channelNumber(String channel) {

        int channelNumber;
        assert channel != null;
        if (channel.equals( "ignore" ) || channel.equals( "select" ) ) {

            IJ.error("No measure channel selected.");
            channelNumber = 0;

        } else {

            channelNumber = Integer.parseInt( channel );

        }

        return channelNumber;

    }



}
