/*
 * Copyright 2021 Delft University of Technology
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.f4sten.pomanalyzer;

import com.beust.jcommander.Parameter;

import eu.f4sten.server.core.DefaultKafkaTopics;

public class MyArgs {

	@Parameter(names = "--pomanalyzer.kafkaIn")
	public String kafkaIn = DefaultKafkaTopics.ANOTHER;

	@Parameter(names = "--pomanalyzer.kafkaOut")
	public String kafkaOut = DefaultKafkaTopics.POM_ANALYZER;

	@Parameter // keep this to prevent crashes when a main arg is provided
	public String main;
}