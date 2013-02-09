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

package org.openvoters;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.infinispan.*;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.loaders.CacheLoaderException;
import org.infinispan.loaders.CacheLoaderManager;
import org.infinispan.loaders.CacheStore;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.MIMECacheEntry;
import org.openvoters.utils.OVUtils;

public class VoteHandler {
	private static final String VOTERS_CACHE_URL = OVUtils.BASE_URL
			+ "/rest/openvoters/";
	private Logger LOGGER = Logger.getLogger("VoteHandler");
	private Cache<String, String> cache;

	public VoteHandler() {
		LOGGER.setLevel(OVUtils.LEVEL);
		cache = OVUtils.getOpenvotersCache();
	}

	public void putMethod(String urlServerAddress, String value)
			throws IOException {
		LOGGER.info("----------------------------------------");
		LOGGER.info("Executing PUT");
		LOGGER.info("----------------------------------------");
		URL address = new URL(urlServerAddress);
		LOGGER.info("executing request " + urlServerAddress);
		HttpURLConnection connection = (HttpURLConnection) address
				.openConnection();
		LOGGER.info("Executing put method of value: " + value);
		connection.setRequestMethod("PUT");
		connection.setRequestProperty("Content-Type", "text/plain");
		connection.setDoOutput(true);

		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
				connection.getOutputStream());
		outputStreamWriter.write(value);

		connection.connect();
		outputStreamWriter.flush();

		LOGGER.info("----------------------------------------");
		LOGGER.info(connection.getResponseCode() + " "
				+ connection.getResponseMessage());
		LOGGER.info("----------------------------------------");

		connection.disconnect();
	}

	public String getMethod(String urlServerAddress) throws IOException {
		String line = new String();
		StringBuilder stringBuilder = new StringBuilder();

		LOGGER.info("----------------------------------------");
		LOGGER.info("Executing GET");
		LOGGER.info("----------------------------------------");

		URL address = new URL(urlServerAddress);
		LOGGER.info("executing request " + urlServerAddress);

		HttpURLConnection connection = (HttpURLConnection) address
				.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Content-Type", "text/plain");
		connection.setDoOutput(true);

		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(connection.getInputStream()));

		connection.connect();

		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line);
		}

		LOGGER.info("Executing get method of value: ["
				+ stringBuilder.toString() + "]");

		LOGGER.info("----------------------------------------");
		LOGGER.info(connection.getResponseCode() + " "
				+ connection.getResponseMessage());
		LOGGER.info("----------------------------------------");

		connection.disconnect();

		return stringBuilder.toString();
	}

	public void voteFor(String myID, String candidate) throws IOException {
		RankingHandler rh = new RankingHandler();
		String oldCandidate = "";
		try {
			oldCandidate = getMethod(VOTERS_CACHE_URL + myID);
		} catch (FileNotFoundException fnfe) {

		}
		putMethod(VOTERS_CACHE_URL + myID, candidate);
		rh.updateRanking(candidate, oldCandidate);
	}

	public String readValue(String myID) throws IOException {
		String result = "";
		result = getMethod(VOTERS_CACHE_URL + myID);
		return result;
	}

	public Map<String, Integer> readValues() throws IOException {
		CacheStore s = cache.getAdvancedCache().getComponentRegistry()
				.getComponent(CacheLoaderManager.class).getCacheStore();

		HashMap<String, Integer> hm = new HashMap<String, Integer>();

		Map<String, Integer> sortedMapAsc = null;
		try {
			Set<InternalCacheEntry> set = s.loadAll();
			Iterator<InternalCacheEntry> i = set.iterator();
			while (i.hasNext()) {
				InternalCacheEntry ice = (InternalCacheEntry) i.next();
				String key = (String) ice.getKey().toString();
				MIMECacheEntry mimevalue = (MIMECacheEntry) ice.getValue();
				String value = new String(mimevalue.data);

				if (hm.containsKey(value)) {
					int count = ((Integer) hm.get(value)).intValue();
					hm.put(value, new Integer(count + 1));
				} else {
					hm.put(value, new Integer(1));
				}
			}

			sortedMapAsc = sortByComparator(hm, false);

		} catch (CacheLoaderException e) {
			e.printStackTrace();
			LOGGER.info("error: " + e.toString());

		}
		return sortedMapAsc;
	}

	public static Map<String, Integer> sortByComparator(
			Map<String, Integer> unsortMap, final boolean order) {

		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(
				unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2) {
				if (order) {
					return o1.getValue().compareTo(o2.getValue());
				} else {
					return o2.getValue().compareTo(o1.getValue());

				}
			}
		});

		// Maintaining insertion order with the help of LinkedList
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Entry<String, Integer> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public String printMap(Map<String, Integer> map) {
		StringBuffer sb = new StringBuffer();
		for (Entry<String, Integer> entry : map.entrySet()) {
			sb.append("Key : " + entry.getKey() + " Value : "
					+ entry.getValue());
		}
		return sb.toString();
	}

}