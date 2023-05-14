/**
 *   POM (xml file)
 *        |
 *        |   Parse XML
 *        V
 *     PomInfo
 *        |
 *        |   Fetch all parent poms
 *        V
 * ChildHavingPomInfo
 *        |
 *        |   Squash parent poms and replace properties
 *        V
 * EffectivePomInfo
 *        |
 *        |   Fetch BOMs and other deps with import scope
 *        v
 *      ?????
 *        |
 *        |   Collapse dependency and dependency-management
 *        v
 *      PomManifest  [ contains just list of deps ]
 *        |
 *        |   Normalize snapshots and version ranges
 *        v
 *      PomManifest  [ type unchanged ]
 */
package dev.mccue.resolve.maven;