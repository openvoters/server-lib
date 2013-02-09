/*  Open Voters - your opinion counts.
 *  Copyright (C) 2013 OpenVoters.org 
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openvoters.utils;


import java.io.IOException;
import java.util.logging.Level;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class OVUtils extends OVConfig {
    
    private static EmbeddedCacheManager manager;
    
    private static Cache<String, String> rankingCache;
    private static final String RANKING_CACHE_NAME = "ranking";
    private static Cache<String, String> openvotersCache;
    private static final String LIST_CACHE_NAME = "list";
    private static Cache<String, String> listCache;
    private static final String OPENVOTERS_CACHE_NAME = "openvoters";
    
    public static Cache<String,String> getRankingCache() {
        if (manager==null) {
            try {
                manager = new DefaultCacheManager(CACHE_CONFIG_FILENAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        rankingCache = manager.getCache(RANKING_CACHE_NAME);
        return rankingCache;
    }
    
    public static Cache<String,String> getListCache() {
        if (manager==null) {
            try {
                manager = new DefaultCacheManager(CACHE_CONFIG_FILENAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        listCache= manager.getCache(LIST_CACHE_NAME);
        return listCache;
    }
    
    public static Cache<String,String> getOpenvotersCache() {
        if (manager==null) {
            try {
                manager = new DefaultCacheManager(CACHE_CONFIG_FILENAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        openvotersCache = manager.getCache(OPENVOTERS_CACHE_NAME);
        return openvotersCache;
    }
    

}
