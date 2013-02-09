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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.infinispan.*;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.loaders.CacheLoaderException;
import org.infinispan.loaders.CacheLoaderManager;
import org.infinispan.loaders.CacheStore;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.MIMECacheEntry;
import org.openvoters.utils.OVUtils;

import com.google.gson.Gson;


public class RankingHandler {
	private static final String RANKING_CACHE_URL = OVUtils.BASE_URL
			+ "/rest/ranking/";
	private Logger LOGGER = Logger.getLogger("RankingHandler");
	private Cache<String, String> cache;

	public RankingHandler() {
		LOGGER.setLevel(OVUtils.LEVEL);
		cache = OVUtils.getRankingCache();
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
		connection.setRequestProperty("Content-Type", "application/json");
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
		connection.setRequestProperty("Content-Type", "application/json");
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
				hm.put(key, new Integer(value));
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

	public String readValue(String myID) throws IOException {
		String result = "";
		result = getMethod(RANKING_CACHE_URL + myID);
		return result;
	}

	public void updateRanking(String candidate, String oldCandidate)
			throws IOException {
		String votes = "";

		try {
			votes = getMethod(RANKING_CACHE_URL + candidate);
			LOGGER.info("[" + candidate + "] votes:" + votes);

			int v = Integer.parseInt(votes);
			v += 1;
			String newVotes = "" + v;
			LOGGER.info("[" + candidate + "] new votes:" + newVotes);

			putMethod(RANKING_CACHE_URL + candidate, newVotes);
		} catch (FileNotFoundException fnfe) {
			LOGGER.info("First insert for: " + candidate);
			putMethod(RANKING_CACHE_URL + candidate, "" + 1);
		}

		if (!oldCandidate.equals("")) {
			updateOldCandidateVotes(oldCandidate);
		}

	}

	private void updateOldCandidateVotes(String oldCandidate)
			throws IOException {
		String oldVotes = getMethod(RANKING_CACHE_URL + oldCandidate);
		LOGGER.info("[" + oldCandidate + "] oldVotes:" + oldVotes);
		int ov = Integer.parseInt(oldVotes);
		ov -= 1;
		String newVotesOldCandidate = "" + ov;
		LOGGER.info("[" + oldCandidate + "] new votes:" + newVotesOldCandidate);
		putMethod(RANKING_CACHE_URL + oldCandidate, newVotesOldCandidate);
	}

	public String getRanking() throws IOException {
		String list = readValues().toString();
		return list;
	}

	public String getJsonRanking() throws IOException {
		Gson gson = new Gson();
		return gson.toJson(readValues());
	}
}