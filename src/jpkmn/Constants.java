package jpkmn;

import java.util.prefs.Preferences;

public class Constants {
  // Later incorporated into server settings
  public static final int BOXSIZE = 20;
  public static final int BOXNUMBER = 8;
  public static final int PARTYSIZE = 6;
  public static final int MOVENUMBER = 4;
  public static final int STATCHANGEMAX = 6;
  public static final double TYPEADVANTAGE = 2.0;
  public static final Preferences prefs = Preferences
      .systemNodeForPackage(Constants.class);
}